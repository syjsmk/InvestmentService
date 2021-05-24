package com.syjsmk.investmentservice.service.impl;

import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.InvestResponseDTO;
import com.syjsmk.investmentservice.model.InvestmentGoodsVO;
import com.syjsmk.investmentservice.model.UserInvestmentGoodsVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class InvestmentServiceImplTest {

    @Autowired
    private InvestmentServiceImpl investmentService;

    @Autowired
    private R2dbcEntityTemplate template;

    @BeforeEach
    public void setUp() {

        template.getDatabaseClient().sql("CREATE TABLE investment_goods( " +
                "goods_id serial PRIMARY KEY," +
                "title varchar(30) NOT NULL," +
                "total_investing_amount bigint," +
                "status bool," +
                "started_at timestamp," +
                "finished_at timestamp" +
                ")")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("CREATE TABLE user_investment_goods(" +
                "id serial PRIMARY KEY," +
                "user_id integer," +
                "goods_id integer," +
                "user_investing_amount bigint," +
                "invest_date timestamp" +
                ");")
                .fetch().rowsUpdated().block();


        template.getDatabaseClient().sql("INSERT INTO investment_goods " +
                "(goods_id, title, total_investing_amount, status, started_at, finished_at) " +
                "VALUES (1, '개인신용 포트폴리오', 100000, true, '2021-04-02 00:00:00.000000', '2021-04-30 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO investment_goods " +
                "(goods_id, title, total_investing_amount, status, started_at, finished_at) " +
                "VALUES (2, '부동산 포트폴리오', 200000, true, '2021-05-02 00:00:00.000000', '2021-05-30 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO user_investment_goods  " +
                "(user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (1, 1, 10000, '2021-04-06 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO user_investment_goods  " +
                "(user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (1, 2, 20000, '2021-04-07 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO user_investment_goods  " +
                "(user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (2, 1, 30000, '2021-04-07 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO user_investment_goods  " +
                "(user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (2, 2, 40000, '2021-04-07 00:00:00.000000');")
                .fetch().rowsUpdated().block();
    }

    @AfterEach
    public void tearDown() {
        template.getDatabaseClient().sql("DROP TABLE investment_goods;")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("DROP TABLE user_investment_goods;")
                .fetch().rowsUpdated().block();
    }


    @Test
    @DisplayName(value = "전체 투자 상품 조회 API - 시작/종료 날짜가 날짜의 형식에 맞지 않는 경우")
    public void selectAllInvestmentGoodsSelectParsingError(TestInfo testInfo) {

        assertThrows(DateTimeParseException.class, () -> investmentService.selectAllInvestmentGoods(
                "2021-01-01 00:00:00", "2021"
        ).collectList().block());
    }

    @Test
    @DisplayName(value = "전체 투자 상품 조회 API - 일부만 조회되는 경우")
    public void selectAllInvestmentGoodsPartial(TestInfo testInfo) {

        List<InvestmentGoodsVO> result = investmentService.selectAllInvestmentGoods(
                "2021-04-01 00:00:00", "2021-04-30 00:00:00"
        ).collectList().block();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName(value = "전체 투자 상품 조회 API - DB에 있는 데이터의 상품 모집기간 외의 데이터를 조회")
    public void selectAllInvestmentGoodsSelectNothing(TestInfo testInfo) {

        List<InvestmentGoodsVO> result = investmentService.selectAllInvestmentGoods(
                "2021-01-01 00:00:00", "2021-01-30 00:00:00"
        ).collectList().block();

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName(value = "전체 투자 상품 조회 API - DB에 있는 모든 데이터를 조회")
    public void selectAllInvestmentGoodsSelectSuccess(TestInfo testInfo) {

        List<InvestmentGoodsVO> result = investmentService.selectAllInvestmentGoods(
                "2021-04-01 00:00:00", "2021-06-30 00:00:00"
        ).collectList().block();

        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getGoodsId());
        assertEquals(2, result.get(0).getInvestorCount());
        assertEquals(40000, result.get(0).getCurrentInvestingAmount());

        assertEquals(2, result.get(1).getGoodsId());
        assertEquals(2, result.get(1).getInvestorCount());
        assertEquals(60000, result.get(1).getCurrentInvestingAmount());
    }

    @Test
    @DisplayName(value = "투자하기 API - 투지 후 투자자 수/투자 금액 변화")
    public void investTestSelectResult(TestInfo testInfo) {

        investmentService.invest(3, 1, 10000L).block();

        List<InvestmentGoodsVO> result = result = investmentService.selectAllInvestmentGoods(
                "2021-04-01 00:00:00", "2021-06-30 00:00:00"
        ).collectList().block();

        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getGoodsId());
        assertEquals(3, result.get(0).getInvestorCount());
        assertEquals(50000, result.get(0).getCurrentInvestingAmount());
        assertEquals(true, result.get(0).getStatus());

        InvestResponseDTO insertResult = investmentService.invest(4, 1, 200000L).block();

        assertEquals(50000, insertResult.getInvestmentAmount());

        result = investmentService.selectAllInvestmentGoods(
                "2021-04-01 00:00:00", "2021-06-30 00:00:00"
        ).collectList().block();

        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getGoodsId());
        assertEquals(4, result.get(0).getInvestorCount());
        assertEquals(100000, result.get(0).getCurrentInvestingAmount());
        assertEquals(false, result.get(0).getStatus());

    }

    @Test
    @DisplayName(value = "투자하기 API - 존재하지 않는 상품에 투자")
    public void investTestProductIdNotExist(TestInfo testInfo) {

        InvestResponseDTO result = investmentService.invest(1, 50, 10000L).block();

        assertEquals(null, result);
    }

    @Test
    @DisplayName(value = "투자하기 API - 투자하기 성공")
    public void investTestSuccess(TestInfo testInfo) {

        InvestResponseDTO result = investmentService.invest(1, 1, 10000L).block();

        assertEquals(1, result.getUserId());
        assertEquals(1, result.getGoodsId());
        assertEquals(100000, result.getTotalInvestingAmount());
        assertEquals(true, result.getStatus());
        assertEquals(10000, result.getInvestmentAmount());

    }


    @Test
    @DisplayName(value = "나의 투자상품 조회 API - 존재하지 않는 유저 ID조회")
    public void selectUserInvestmentGoodsUserIdNotExist(TestInfo testInfo) {

        List<UserInvestmentGoodsVO> result = investmentService.selectUserInvestmentGoods(99).collectList().block();

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName(value = "나의 투자상품 조회 API - 나의 ID에 해당하는 데이터 조회")
    public void selectUserInvestmentGoodsSuccess(TestInfo testInfo) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.dateTimePattern);

        List<UserInvestmentGoodsVO> result = investmentService.selectUserInvestmentGoods(1).collectList().block();

        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getGoodsId());
        assertEquals("개인신용 포트폴리오", result.get(0).getTitle());
        assertEquals(100000, result.get(0).getTotalInvestingAmount());
        assertEquals(10000, result.get(0).getUserInvestingAmount());
        assertEquals(LocalDateTime.parse("2021-04-06 00:00:00", formatter), result.get(0).getInvestDate());


        assertEquals(2, result.get(1).getGoodsId());
        assertEquals("부동산 포트폴리오", result.get(1).getTitle());
        assertEquals(200000, result.get(1).getTotalInvestingAmount());
        assertEquals(20000, result.get(1).getUserInvestingAmount());
        assertEquals(LocalDateTime.parse("2021-04-07 00:00:00", formatter), result.get(1).getInvestDate());
    }



}
