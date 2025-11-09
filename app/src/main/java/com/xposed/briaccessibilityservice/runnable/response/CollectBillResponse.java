package com.xposed.briaccessibilityservice.runnable.response;

import lombok.Data;

@Data
public class CollectBillResponse {
    private Long id;

    //转账订单号
    private long uid;

    //设备卡号
    private String cardNumber;

    //目标卡号
    private String phone;

    //银行编码
    private String bank;

    //转账金额
    private long idPlgn;

    //状态 0=处理中 1=转账成功 2=转账失败
    private int state;

    //转账类型 0=钱包转账 1=普通转账
    private int type;

    //失败原因
    private String error;

    //创建时间
    private String createDate;
}
