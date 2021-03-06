package com.syjsmk.investmentservice.controller;

import com.syjsmk.investmentservice.common.Const;
import com.syjsmk.investmentservice.model.InvestRequestDTO;
import com.syjsmk.investmentservice.model.InvestResponseDTO;
import com.syjsmk.investmentservice.model.UserInvestmentGoods;
import com.syjsmk.investmentservice.model.UserInvestmentGoodsVO;
import com.syjsmk.investmentservice.service.InvestmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/api/investment")
public class InvestmentServiceController {

    final InvestmentService investmentService;

    public InvestmentServiceController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping("/goods")
    public Mono<ResponseEntity<?>> selectAllInvestmentGoods(
            @RequestParam(value = Const.Params.startedAt) String startedAt,
            @RequestParam(value = Const.Params.finishedAt) String finishedAt
    ) {
        return investmentService.selectAllInvestmentGoods(startedAt, finishedAt)
                .collectList()
                .map(investmentGoodsVOS -> {
                    if(investmentGoodsVOS.size() > 0) {
                        return ResponseEntity.ok().body(investmentGoodsVOS);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                })
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/user/goods")
    public Mono<ResponseEntity<InvestResponseDTO>> invest(
            @RequestHeader(Const.Params.xUserId) Integer userId,
            @RequestBody InvestRequestDTO investRequestDto
    ) {
        return investmentService.invest(userId, investRequestDto.getGoodsId(), investRequestDto.getInvestmentAmount())
                .map(userInvestmentGoods -> ResponseEntity.ok().body(userInvestmentGoods))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorReturn(ResponseEntity.badRequest().build());

    }

    @GetMapping("/user/goods")
    public Mono<ResponseEntity<List<UserInvestmentGoodsVO>>> selectUserInvestmentGoods(
            @RequestHeader(Const.Params.xUserId) Integer userId
    ) {
        return investmentService.selectUserInvestmentGoods(userId)
                .collectList()
                .map(userInvestmentGoodsVO -> {
                    if(userInvestmentGoodsVO.size() > 0) {
                        return ResponseEntity.ok().body(userInvestmentGoodsVO);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

}
