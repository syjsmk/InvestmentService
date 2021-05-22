package com.syjsmk.investmentservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.syjsmk.investmentservice.common.Const;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@SuperBuilder
@ToString
@NoArgsConstructor
@Data
public class UserInvestmentGoodsVO extends BaseInvestmentGoodsVO {

    // 나의 투자금액
    private Long userInvestingAmount;

    // 투자일시
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Const.dateTimePattern)
    private LocalDateTime investDate;

}
