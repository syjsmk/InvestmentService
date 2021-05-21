package com.syjsmk.investmentservice.service;

import com.syjsmk.investmentservice.model.InvestmentGoodsVO;
import com.syjsmk.investmentservice.model.UserInvestmentGoods;
import com.syjsmk.investmentservice.model.UserInvestmentGoodsVO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InvestmentService {

    public Flux<InvestmentGoodsVO> selectAllInvestmentGoods(String startedAt, String finishedAt);
    public Mono<UserInvestmentGoods> invest(Integer userId, Integer goodsId, Long investmentAmount);
    public Flux<UserInvestmentGoodsVO> selectUserInvestmentGoods(Integer userId);

}
