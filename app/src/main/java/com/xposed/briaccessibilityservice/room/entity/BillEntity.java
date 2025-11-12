package com.xposed.briaccessibilityservice.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//收款账单
@Entity(tableName = "bill")
public class BillEntity {
    // 主键
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public long uid;

    //金额
    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "name")
    private String name; //Transfer Ke RAGA AJAIBAN via BRImo

    @ColumnInfo(name = "money")
    private String money;//- Rp15.000,00 + Rp500.000,00

    @ColumnInfo(name = "state")
    private int state;//状态 0=提交失败 1=提交成功

    @ColumnInfo(name = "time")
    private String time;//收款时间

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
