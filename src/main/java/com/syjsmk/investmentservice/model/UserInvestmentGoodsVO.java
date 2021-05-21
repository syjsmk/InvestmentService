package com.syjsmk.investmentservice.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@ToString
@NoArgsConstructor
public class UserInvestmentGoodsVO extends BaseInvestmentGoodsVO {

    // 나의 투자금액
    private Long userInvestingAmount;

    // 투자일시
    private LocalDateTime investDate;

}
