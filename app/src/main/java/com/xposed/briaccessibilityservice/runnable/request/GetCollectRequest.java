package com.xposed.briaccessibilityservice.runnable.request;


import lombok.Data;

@Data
public class GetCollectRequest {
    //设备卡号
    private String cardNumber;
    //设备余额
    private long balance;
}
