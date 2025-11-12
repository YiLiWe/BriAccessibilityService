package com.xposed.briaccessibilityservice.runnable.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BriBillRequest {
    //原文
    private String text;
    private String name; //Transfer Ke RAGA AJAIBAN via BRImo
    private String money;//- Rp15.000,00 + Rp500.000,00
    private String time;//收款时间
}
