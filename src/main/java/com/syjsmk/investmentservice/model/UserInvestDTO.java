package com.syjsmk.investmentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInvestDTO {

    // 상품 id
    @JsonProperty("goodsId")
    private Integer goodsId;

    // 투자 금액
    @JsonProperty("investmentAmount")
    private Long investmentAmount;

}
