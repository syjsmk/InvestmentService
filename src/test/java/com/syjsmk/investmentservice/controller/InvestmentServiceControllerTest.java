package com.syjsmk.investmentservice.controller;

import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.*;
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
import reactor.core.publisher.Mono;

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
    @DisplayName(value = "전체 투자 상품 조회 API - 조회 기간에 해당하는 데이터가 없음")
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
    @DisplayName(value = "전체 투자 상품 조회 API - 잘못된 파라미터로 요청")
    public void testSelectAllInvestmentWithoutParameter(TestInfo testInfo) throws Exception {

        Flux<InvestmentGoodsVO> mockDatas = Flux.error(new DateTimeParseException("message", "parsedData", 1));

        when(investmentService.selectAllInvestmentGoods(Mockito.any(), Mockito.any())).thenReturn(mockDatas);

        webTestClient.get().uri("/v1/api/investment/goods?started_at=2021-04-01 00:00:00&finished_at=5")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    @DisplayName(value = "전체 투자 상품 조회 API - 조회 성공")
    public void testSelectAllInvestmentGoodsSuccess(TestInfo testInfo) throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.dateTimePattern);

        Flux<InvestmentGoodsVO> mockDatas = Flux.just(
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

    @Test
    @DisplayName(value = "투자하기 API - X-USER-ID 값이 헤더에 존재하지 않음")
    public void testInvestUserIdNotExist(TestInfo testInfo) throws Exception {

        Mono<InvestRequestDTO> mockRequest = Mono.just(InvestRequestDTO.builder()
                .goodsId(1)
                .investmentAmount(1000L)
                .build());

        webTestClient.post().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "")
                .body(mockRequest, InvestRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    @DisplayName(value = "투자하기 API - 잘못된 인자로 요청")
    public void testInvestToBadRequest(TestInfo testInfo) throws Exception {

        Mono<InvestResponseDTO> mockResult = Mono.error(new IllegalArgumentException());

        Mono<InvestRequestDTO> mockRequest = Mono.just(InvestRequestDTO.builder()
                .goodsId(1)
                .build());

        when(investmentService.invest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mockResult);

        webTestClient.post().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "1")
                .body(mockRequest, InvestRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    @DisplayName(value = "투자하기 API - 존재하지 않는 상품에 투자")
    public void testInvestToNotExistGoods(TestInfo testInfo) throws Exception {

        Mono<InvestResponseDTO> mockResult = Mono.empty();

        Mono<InvestRequestDTO> mockRequest = Mono.just(InvestRequestDTO.builder()
                .goodsId(1)
                .investmentAmount(1000L)
                .build());

        when(investmentService.invest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mockResult);

        webTestClient.post().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "1")
                .body(mockRequest, InvestRequestDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    @DisplayName(value = "투자하기 API - 모집 종료된 상품에 투자")
    public void testInvestToStatusFalse(TestInfo testInfo) throws Exception {

        Mono<InvestResponseDTO> mockResult = Mono.empty();

        Mono<InvestRequestDTO> mockRequest = Mono.just(InvestRequestDTO.builder()
                .goodsId(1)
                .investmentAmount(1000L)
                .build());

        when(investmentService.invest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mockResult);

        webTestClient.post().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "1")
                .body(mockRequest, InvestRequestDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    @DisplayName(value = "투자하기 API - 투자하기 성공")
    public void testInvestSuccess(TestInfo testInfo) throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.dateTimePattern);

        Mono<InvestResponseDTO> mockResult = Mono.just(InvestResponseDTO.builder()
                .userId(1)
                .goodsId(1)
                .totalInvestingAmount(100000L)
                .investmentAmount(1000L)
                .status(true)
                .investDate(LocalDateTime.parse("2021-05-22 18:25:54", formatter))
                .build());

        Mono<InvestRequestDTO> mockRequest = Mono.just(InvestRequestDTO.builder()
                .goodsId(1)
                .investmentAmount(1000L)
                .build());

        when(investmentService.invest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mockResult);

        webTestClient.post().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "1")
                .body(mockRequest, InvestRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(1)
                .jsonPath("$.goodsId").isEqualTo(1)
                .jsonPath("$.investmentAmount").isEqualTo(1000)
                .jsonPath("$.investDate").isEqualTo("2021-05-22 18:25:54");
    }


    @Test
    @DisplayName(value = "나의 투자상품 조회 API - X-USER-ID에 해당하는 데이터가 없음")
    public void testSelectUserInvestmentGoodsUserIdNotExist(TestInfo testInfo) throws Exception {

        Flux<UserInvestmentGoodsVO> mockDatas = Flux.empty();

        when(investmentService.selectUserInvestmentGoods(Mockito.any())).thenReturn(mockDatas);

        webTestClient.get().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "55")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName(value = "나의 투자상품 조회 API - 잘못된 파라미터로 요청")
    public void testSelectUserInvestmentGoodsWithoutParameter(TestInfo testInfo) throws Exception {

        webTestClient.get().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "dd")
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    @DisplayName(value = "나의 투자상품 조회 API - 조회 성공")
    public void testSelectUserInvestmentGoodsSuccess(TestInfo testInfo) throws Exception {

        Flux<UserInvestmentGoodsVO> mockDatas = Flux.just(
                UserInvestmentGoodsVO.builder()
                        .goodsId(1)
                        .title("t1")
                        .totalInvestingAmount(100000L)
                        .build(),
                UserInvestmentGoodsVO.builder()
                        .goodsId(2)
                        .title("t2")
                        .totalInvestingAmount(200000L)
                        .build()
        );

        when(investmentService.selectUserInvestmentGoods(Mockito.any())).thenReturn(mockDatas);

        webTestClient.get().uri("/v1/api/investment/user/goods")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", "1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserInvestmentGoodsVO.class);

        Mockito.verify(investmentService, times(1))
                .selectUserInvestmentGoods(1);

    }

}
