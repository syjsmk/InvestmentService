package com.syjsmk.investmentservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.syjsmk.investmentservice.common.Const;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
public class InvestResponseDTO {

    private Integer userId;

    private Integer goodsId;

    // 총 투자 모집금액
    private Long totalInvestingAmount;

    private Long investmentAmount;

    // 투자모집상태 (모집중, 모집완료)
    private Boolean status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Const.dateTimePattern)
    private LocalDateTime investDate;

}
