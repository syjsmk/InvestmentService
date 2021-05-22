package com.syjsmk.investmentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.BaseInvestmentGoodsVO;
import com.syjsmk.investmentservice.model.InvestmentGoodsVO;
import com.syjsmk.investmentservice.model.UserInvestDTO;
import com.syjsmk.investmentservice.model.UserInvestmentGoods;
import com.syjsmk.investmentservice.service.InvestmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebFluxTest(InvestmentServiceController.class)
@Import(InvestmentService.class)
public class InvestmentServiceControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private InvestmentService investmentService;

    @Test
    @DisplayName(value = "조회 기간에 해당하는 데이터가 없음")
    public void testSelectAllInvestmentNoDataInPeriod(TestInfo testInfo) throws Exception {

        Flux<InvestmentGoodsVO> mockDatas = Flux.empty();

        when(investmentService.selectAllInvestmentGoods(Mockito.any(), Mockito.any())).thenReturn(mockDatas);

        webTestClient.get().uri("/v1/api/investment/goods?started_at=2021-04-01 00:00:00&finished_at=2021-05-31 00:00:00")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        Mockito.verify(investmentService, times(1))
                .selectAllInvestmentGoods("2021-04-01 00:00:00", "2021-05-31 00:00:00");

    }

    @Test
    @DisplayName(value = "잘못된 파라미터로 요청")
    public void testSelectAllInvestmentWithoutParameter(TestInfo testInfo) throws Exception {

        Flux<InvestmentGoodsVO> mockDatas = Flux.error(new DateTimeParseException("message", "parsedData", 1));

        when(investmentService.selectAllInvestmentGoods(Mockito.any(), Mockito.any())).thenReturn(mockDatas);

        webTestClient.get().uri("/v1/api/investment/goods?started_at=2021-04-01 00:00:00&finished_at=5")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verify(investmentService, times(1))
                .selectAllInvestmentGoods("2021-04-01 00:00:00", "5");

    }

    @Test
    @DisplayName(value = "데이터 조회 성공")
    public void testSelectAllInvestmentGoodsSuccess(TestInfo testInfo) throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.dateTimePattern);

        var mockDatas = Flux.just(
                InvestmentGoodsVO.builder()
                        .goodsId(1)
                        .title("t1")
                        .totalInvestingAmount(100000L)
                        .currentInvestingAmount(80000L)
                        .investorCount(2)
                        .status(true)
                        .startedAt(LocalDateTime.parse("2021-04-02 14:27:38", formatter))
                        .finishedAt(LocalDateTime.parse("2021-04-29 14:27:44", formatter))
                        .build(),
                InvestmentGoodsVO.builder()
                        .goodsId(2)
                        .title("t2")
                        .totalInvestingAmount(200000L)
                        .currentInvestingAmount(90000L)
                        .investorCount(1)
                        .status(false)
                        .startedAt(LocalDateTime.parse("2021-05-02 14:28:20", formatter))
                        .finishedAt(LocalDateTime.parse("2021-05-30 14:28:25", formatter))
                        .build()
        );

        when(investmentService.selectAllInvestmentGoods(Mockito.any(), Mockito.any())).thenReturn(mockDatas);

        webTestClient.get().uri("/v1/api/investment/goods?started_at=2021-04-01 00:00:00&finished_at=2021-05-31 00:00:00")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InvestmentGoodsVO.class);

        Mockito.verify(investmentService, times(1))
                .selectAllInvestmentGoods("2021-04-01 00:00:00", "2021-05-31 00:00:00");

    }

}
