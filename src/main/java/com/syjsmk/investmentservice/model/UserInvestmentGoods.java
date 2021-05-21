package com.syjsmk.investmentservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.syjsmk.investmentservice.common.Const;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

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

    private LocalDateTime investDate;

}
