package com.syjsmk.investmentservice.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@ToString
@SuperBuilder
@NoArgsConstructor
public class BaseInvestmentGoodsVO {

    // 상품 id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer goodsId;

    // 투자명
    private String title;

    // 총 투자 모집금액
    private Long totalInvestingAmount;

}
