package com.syjsmk.investmentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.InvestmentGoodsVO;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
//@DataR2dbcTest
public class InvestmentServiceImplTest {

    @Spy
    @InjectMocks
    private InvestmentServiceImpl investmentService;

    @Test
    public void selectAllInvestmentGoodsSelectNothing(TestInfo testInfo) {

        MockitoAnnotations.initMocks(this);

        var mockDatas = Flux.just();

        doReturn(mockDatas).when(investmentService).selectAllInvestmentGoods(Mockito.any(), Mockito.any());

        var result = investmentService.selectAllInvestmentGoods("2021-03-01 00:00:00", "2021-03-31 00:00:00");

        assertEquals(result.collectList().block().size(), 0);
    }

    @Test
    public void selectAllInvestmentGoodsSuccess(TestInfo testInfo) {

        MockitoAnnotations.initMocks(this);

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

        doReturn(mockDatas).when(investmentService).selectAllInvestmentGoods(Mockito.any(), Mockito.any());

        var result = investmentService.selectAllInvestmentGoods("2021-04-01 00:00:00", "2021-05-31 00:00:00");

        assertEquals(result.collectList().block().size(), 2);
    }

}
