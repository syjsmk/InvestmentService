package com.syjsmk.investmentservice.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class UserInvestmentGoodsVO extends BaseInvestmentGoodsVO {

    // 나의 투자금액
    private Long userInvestingAmount;

    // 투자일시
    private LocalDateTime investDate;

}
