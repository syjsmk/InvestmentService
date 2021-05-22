package com.syjsmk.investmentservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@ToString
@SuperBuilder
@NoArgsConstructor
public class InvestmentGoodsVO extends BaseInvestmentGoodsVO {

    // 현재 모집금액
    private Long currentInvestingAmount;

    // 투자자 수
    private Integer investorCount;

    // 투자모집상태 (모집중, 모집완료)
    private Boolean status;

    // 투자시작일시
    private LocalDateTime startedAt;

    // 투자종료일시
    private LocalDateTime finishedAt;
}
