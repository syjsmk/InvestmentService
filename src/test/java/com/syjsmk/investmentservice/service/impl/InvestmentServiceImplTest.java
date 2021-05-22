package com.syjsmk.investmentservice.service.impl;

import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.InvestmentGoodsVO;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InvestmentServiceImplTest {

//    @Spy
//    @InjectMocks
//    private InvestmentServiceImpl investmentService;
//
//    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
//    private R2dbcEntityTemplate template;
//
//    @Mock
//    private ObjectMapper objectMapper;

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
                "(id, user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (1, 1, 1, 10000, '2021-04-06 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO user_investment_goods  " +
                "(id, user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (2, 1, 2, 20000, '2021-04-07 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO user_investment_goods  " +
                "(id, user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (3, 2, 1, 30000, '2021-04-07 00:00:00.000000');")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("INSERT INTO user_investment_goods  " +
                "(id, user_id, goods_id, user_investing_amount, invest_date) " +
                "VALUES (4, 2, 2, 40000, '2021-04-07 00:00:00.000000');")
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

        assertEquals(result.size(), 1);
    }

    @Test
    @DisplayName(value = "전체 투자 상품 조회 API - DB에 있는 데이터의 상품 모집기간 외의 데이터를 조회")
    public void selectAllInvestmentGoodsSelectNothing(TestInfo testInfo) {

        List<InvestmentGoodsVO> result = investmentService.selectAllInvestmentGoods(
                "2021-01-01 00:00:00", "2021-01-30 00:00:00"
        ).collectList().block();

        assertEquals(result.size(), 0);
    }

    @Test
    @DisplayName(value = "전체 투자 상품 조회 API - DB에 있는 모든 데이터를 조회")
    public void selectAllInvestmentGoodsSelectSuccess(TestInfo testInfo) {

        List<InvestmentGoodsVO> result = investmentService.selectAllInvestmentGoods(
                "2021-04-01 00:00:00", "2021-06-30 00:00:00"
        ).collectList().block();

        assertEquals(result.size(), 2);

        assertEquals(result.get(0).getGoodsId(), 1);
        assertEquals(result.get(0).getInvestorCount(), 2);
        assertEquals(result.get(0).getCurrentInvestingAmount(), 40000);

        assertEquals(result.get(1).getGoodsId(), 2);
        assertEquals(result.get(1).getInvestorCount(), 2);
        assertEquals(result.get(1).getCurrentInvestingAmount(), 60000);
    }

    @AfterEach
    public void tearDown() {
        template.getDatabaseClient().sql("DROP TABLE investment_goods;")
                .fetch().rowsUpdated().block();

        template.getDatabaseClient().sql("DROP TABLE user_investment_goods;")
                .fetch().rowsUpdated().block();
    }

}
