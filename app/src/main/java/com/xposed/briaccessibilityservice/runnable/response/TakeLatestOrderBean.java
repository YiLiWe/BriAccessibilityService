package com.xposed.briaccessibilityservice.runnable.response;

import lombok.Data;

@Data
public class TakeLatestOrderBean {
    private String orderNo;//订单号
    private long amount;//金额
    private String cardNumber;//卡号
    private String bankName;//银行编号
    private String payeeName;
    private String loginPwd;
    private String payPwd;//支付密码
    private boolean isMoney;//判断是否是钱包
}

