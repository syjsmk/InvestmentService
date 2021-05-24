package com.syjsmk.investmentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.InvestResponseDTO;
import com.syjsmk.investmentservice.model.InvestmentGoodsVO;
import com.syjsmk.investmentservice.model.UserInvestmentGoods;
import com.syjsmk.investmentservice.model.UserInvestmentGoodsVO;
import com.syjsmk.investmentservice.service.InvestmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import static org.springframework.data.relational.core.query.Update.update;

@Service
@Slf4j
public class InvestmentServiceImpl implements InvestmentService {

    private final String leftJoinQuery = "SELECT " +
            "    investment_goods.goods_id, " +
            "    investment_goods.title, " +
            "    investment_goods.total_investing_amount, " +
            "    COALESCE(nested.current_investing_amount, 0) AS current_investing_amount, " +
            "    COALESCE(nested.investor_count, 0) AS investor_count, " +
            "    investment_goods.status, " +
            "    to_char(investment_goods.started_at, 'YYYY-MM-DD HH24:MI:SS') AS started_at, " +
            "    to_char(investment_goods.finished_at, 'YYYY-MM-DD HH24:MI:SS') AS finished_at " +
            "FROM " +
            "    investment_goods " +
            "LEFT JOIN " +
            "    ( " +
            "        SELECT " +
            "            goods_id, " +
            "            COUNT (DISTINCT user_id) AS investor_count, " +
            "            SUM(user_investing_amount) AS current_investing_amount " +
            "        FROM " +
            "            user_investment_goods " +
            "        GROUP BY " +
            "            goods_id " +
            "    ) AS nested " +
            "ON investment_goods.goods_id = nested.goods_id ";

    private final ObjectMapper objectMapper;

    private final R2dbcEntityTemplate template;

    public InvestmentServiceImpl(ObjectMapper objectMapper, R2dbcEntityTemplate template) {
        this.objectMapper = objectMapper;
        this.template = template;
    }

    @PostConstruct
    private void setUp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.dateTimePattern);

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        objectMapper.registerModule(javaTimeModule);
    }

    @Override
    public Flux<InvestmentGoodsVO> selectAllInvestmentGoods(String startedAt, String finishedAt) {

        log.info("selectAllInvestmentGoods(startedAt: {}, finishedAt: {})", startedAt, finishedAt);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.dateTimePattern);

            return template.getDatabaseClient()
                    .sql(leftJoinQuery +
                            "WHERE " +
                            "      investment_goods.started_at >= '" + LocalDateTime.parse(startedAt, formatter) + "'" +
                            "AND " +
                            "      investment_goods.finished_at <= '" + LocalDateTime.parse(finishedAt, formatter) + "';")
                    .fetch().all().map(item -> objectMapper.convertValue(item, InvestmentGoodsVO.class));

        } catch(DateTimeParseException e) {
            return Flux.error(e);
        }

    }

    @Override
    @Transactional
    public Mono<InvestResponseDTO> invest(Integer userId, Integer goodsId, Long investmentAmount) {

        log.info("invest(userId: {}, goodsId: {}, investmentAmount: {})", userId, goodsId, investmentAmount);

        if(Objects.isNull(userId)) {
            return Mono.error(new IllegalArgumentException());
        }
        if(Objects.isNull(goodsId)) {
            return Mono.error(new IllegalArgumentException());
        }
        if(Objects.isNull(investmentAmount)) {
            return Mono.error(new IllegalArgumentException());
        }

        return template.getDatabaseClient()
                .sql(leftJoinQuery +
                        "WHERE investment_goods.goods_id = '" + goodsId + "' "
                )
                .fetch().first()
                .map(item -> objectMapper.convertValue(item, InvestmentGoodsVO.class))
                .flatMap(investmentGoodsVO -> {

                    if(!investmentGoodsVO.getStatus()) {
                        return Mono.just(
                                InvestResponseDTO.builder()
                                    .userId(userId)
                                    .goodsId(goodsId)
                                    .totalInvestingAmount(investmentGoodsVO.getTotalInvestingAmount())
                                    .investmentAmount(0L)
                                    .status(investmentGoodsVO.getStatus())
                                    .investDate(LocalDateTime.now())
                                    .build());
                    } else {
                        // 상품의 현재 모집금액 + 투자 금액이 상품의 총 투자모집 금액보다 적은 경우
                        if(investmentGoodsVO.getCurrentInvestingAmount() + investmentAmount < investmentGoodsVO.getTotalInvestingAmount()) {
                            return template.insert(
                                    UserInvestmentGoods.builder()
                                            .userId(userId)
                                            .goodsId(goodsId)
                                            .userInvestingAmount(investmentAmount)
                                            .investDate(LocalDateTime.now())
                                            .build()
                            ).map(userInvestmentGoods -> InvestResponseDTO.builder()
                                    .userId(userId)
                                    .goodsId(goodsId)
                                    .totalInvestingAmount(investmentGoodsVO.getTotalInvestingAmount())
                                    .investmentAmount(userInvestmentGoods.getUserInvestingAmount())
                                    .status(investmentGoodsVO.getStatus())
                                    .investDate(LocalDateTime.now())
                                    .build());
                        } else {
                            // 상품의 현재 모집금액 + 투자 금액이 상품의 총 투자모집 금액과 같거나 큰 경우 해당 상품은 모집 완료 상태가 됨
                            Mono<Integer> update = template.update(InvestmentGoodsVO.class)
                                    .inTable(Const.Tables.investmentGoods)
                                    .matching(query(
                                            where(Const.Fields.goodsId).is(goodsId)
                                    )).apply(update(Const.Fields.status, false));

                            return update.flatMap(u -> template.insert(
                                    UserInvestmentGoods.builder()
                                            .userId(userId)
                                            .goodsId(goodsId)
                                            .userInvestingAmount(investmentGoodsVO.getTotalInvestingAmount() -
                                                    investmentGoodsVO.getCurrentInvestingAmount())
                                            .investDate(LocalDateTime.now())
                                            .build()
                            )).map(userInvestmentGoods -> InvestResponseDTO.builder()
                                    .userId(userId)
                                    .goodsId(goodsId)
                                    .totalInvestingAmount(investmentGoodsVO.getTotalInvestingAmount())
                                    .investmentAmount(userInvestmentGoods.getUserInvestingAmount())
                                    .status(false)
                                    .investDate(LocalDateTime.now())
                                    .build());
                        }
                    }
                });
    }

    @Override
    public Flux<UserInvestmentGoodsVO> selectUserInvestmentGoods(Integer userId) {

        log.info("selectUserInvestmentGoods(userId: {})", userId);

        return template.getDatabaseClient()
                .sql(
                        "SELECT " +
                                "investment_goods.goods_id, " +
                                "investment_goods.title, " +
                                "investment_goods.total_investing_amount, " +
                                "user_investment_goods.user_investing_amount, " +
                                "to_char(user_investment_goods.invest_date, 'YYYY-MM-DD HH24:MI:SS') AS invest_date " +
                                "FROM " +
                                "user_investment_goods, investment_goods " +
                                "WHERE " +
                                "user_investment_goods.goods_id = investment_goods.goods_id " +
                                "AND " +
                                "user_investment_goods.user_id = " + userId
                )
                .fetch().all().map(item -> objectMapper.convertValue(item, UserInvestmentGoodsVO.class));

    }

}
