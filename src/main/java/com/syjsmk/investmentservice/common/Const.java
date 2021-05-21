package com.syjsmk.investmentservice.common;

public class Const {

    public static class Tables {
        public static final String investmentGoods = "investment_goods";
        public static final String userInvestmentGoods = "user_investment_goods";
    }

    public static class Fields {
        public static final String goodsId = "goods_id";
        public static final String status = "status";
    };

    public static class Params {
        public static final String xUserId = "X-USER-ID";
        public static final String startedAt = "started_at";
        public static final String finishedAt = "finished_at";
    }

    public static final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

}
