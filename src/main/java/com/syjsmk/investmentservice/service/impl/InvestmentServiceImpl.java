package com.syjsmk.investmentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.InvestmentGoodsVO;
import com.syjsmk.investmentservice.model.UserInvestmentGoods;
import com.syjsmk.investmentservice.model.UserInvestmentGoodsVO;
import com.syjsmk.investmentservice.service.InvestmentService;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import static org.springframework.data.relational.core.query.Update.update;

@Service
@Slf4j
public class InvestmentServiceImpl implements InvestmentService {

    private final ConnectionFactory connectionFactory;

    private final ObjectMapper objectMapper;

    public InvestmentServiceImpl(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
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

        log.info("selectAllInvestmentGoods: {} | {}", startedAt, finishedAt);

        R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.dateTimePattern);

        return template.getDatabaseClient()
            .sql(
                "SELECT " +
                        "investment_goods.goods_id, " +
                        "investment_goods.title, " +
                        "investment_goods.total_investing_amount, " +
                        "nested.current_investing_amount, " +
                        "nested.investor_count, " +
                        "investment_goods.status, " +
                        "investment_goods.started_at, " +
                        "investment_goods.finished_at " +
                        "FROM " +
                        "( " +
                        "SELECT " +
                        "goods_id, " +
                        "COUNT (DISTINCT user_id) AS investor_count, " +
                        "SUM(user_investing_amount) AS current_investing_amount " +
                        "FROM " +
                        "user_investment_goods " +
                        "GROUP BY " +
                        "goods_id " +
                        ") AS nested, investment_goods " +
                        "WHERE " +
                        "nested.goods_id = investment_goods.goods_id " +
                        "AND " +
                        "investment_goods.started_at >= '" + LocalDateTime.parse(startedAt, formatter) + "' " +
                        "AND " +
                        "investment_goods.finished_at <= '" + LocalDateTime.parse(finishedAt, formatter) + "'"
            )
            .fetch().all().map(item -> objectMapper.convertValue(item, InvestmentGoodsVO.class));

    }

    @Override
    public Mono<UserInvestmentGoods> invest(Integer userId, Integer goodsId, Long investmentAmount) {

        log.info("invest: {} | {} | {}", userId, goodsId, investmentAmount);

        if(userId == null) {
            return Mono.error(new IllegalArgumentException());
        }
        if(goodsId == null) {
            return Mono.error(new IllegalArgumentException());
        }
        if(investmentAmount == null) {
            return Mono.error(new IllegalArgumentException());
        }

        R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);

        return template.getDatabaseClient()
                .sql(
                        "SELECT " +
                                "investment_goods.goods_id, " +
                                "investment_goods.title, " +
                                "investment_goods.total_investing_amount, " +
                                "nested.current_investing_amount, " +
                                "nested.investor_count, " +
                                "investment_goods.status, " +
                                "investment_goods.started_at, " +
                                "investment_goods.finished_at " +
                                "FROM " +
                                "( " +
                                "SELECT " +
                                "goods_id, " +
                                "COUNT (DISTINCT user_id) AS investor_count, " +
                                "SUM(user_investing_amount) AS current_investing_amount " +
                                "FROM " +
                                "user_investment_goods " +
                                "GROUP BY " +
                                "goods_id " +
                                ") AS nested, investment_goods " +
                                "WHERE " +
                                "nested.goods_id = investment_goods.goods_id " +
                                "AND " +
                                "investment_goods.goods_id = '" + goodsId + "' "
                )
                .fetch().first()
                .map(item -> objectMapper.convertValue(item, InvestmentGoodsVO.class))
                .flatMap(investmentGoodsVO -> {

                    if(!investmentGoodsVO.getStatus()) {
                        return Mono.empty();
                    } else {

                        if(investmentGoodsVO.getCurrentInvestingAmount() + investmentAmount < investmentGoodsVO.getTotalInvestingAmount()) {

                            return template.insert(
                                    UserInvestmentGoods.builder()
                                            .userId(userId)
                                            .goodsId(goodsId)
                                            .userInvestingAmount(investmentAmount)
                                            .investDate(LocalDateTime.now())
                                            .build()
                            );
                        } else {

                            Mono<Integer> update = template.update(InvestmentGoodsVO.class)
                                    .inTable(Const.Tables.investmentGoods)
                                    .matching(query(
                                            where(Const.Fields.goodsId).is(goodsId)
                                    )).apply(update(Const.Fields.status, false));

                            return update.flatMap(u -> template.insert(
                                    UserInvestmentGoods.builder()
                                            .userId(userId)
                                            .goodsId(goodsId)
                                            .userInvestingAmount(investmentGoodsVO.getTotalInvestingAmount() - investmentGoodsVO.getCurrentInvestingAmount())
                                            .investDate(LocalDateTime.now())
                                            .build()
                            ));
                        }
                    }
                });
    }

    @Override
    public Flux<UserInvestmentGoodsVO> selectUserInvestmentGoods(Integer userId) {

        log.info("selectUserInvestmentGoods : {}", userId);

        R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);

        return template.getDatabaseClient()
                .sql(
                        "SELECT " +
                                "investment_goods.goods_id, " +
                                "investment_goods.title, " +
                                "investment_goods.total_investing_amount, " +
                                "user_investment_goods.user_investing_amount, " +
                                "user_investment_goods.invest_date " +
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
