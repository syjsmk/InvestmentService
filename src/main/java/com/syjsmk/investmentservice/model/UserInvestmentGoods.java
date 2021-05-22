package com.syjsmk.investmentservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.syjsmk.investmentservice.common.Const;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import java.time.LocalDateTime;

@ToString
@Builder
@Data
@Table(name = Const.Tables.userInvestmentGoods)
public class UserInvestmentGoods {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    private Integer userId;

    private Integer goodsId;

    private Long userInvestingAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-mm-dd HH:mm:ss")
    private LocalDateTime investDate;

}
