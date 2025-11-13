package com.xposed.briaccessibilityservice.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

//提交失败账单
@Entity(tableName = "post_state")
public class PostStateEntity {
    // 主键
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public long uid;

    //错误状态码
    @ColumnInfo(name = "error")
    private String error;

    //账单号
    @ColumnInfo(name = "order_no")
    private String orderNo;

    //状态 0=待提交 1=已提交
    @ColumnInfo(name = "state")
    private int state;

    //0=代付 1=归集
    @ColumnInfo(name = "type")
    private int type;

    //0=待提交 1=提交成功
    @ColumnInfo(name = "post_state")
    private int postState;

    //创建时间
    @ColumnInfo(name = "create_date")
    public Long createDate;

    public static PostStateEntity create(String orderNo, int type, int state, String error) {
        PostStateEntity postStateEntity = new PostStateEntity();
        postStateEntity.setState(state);
        postStateEntity.setOrderNo(orderNo);
        postStateEntity.setPostState(0);
        postStateEntity.setError(error);
        postStateEntity.setCreateDate(System.currentTimeMillis());
        postStateEntity.setType(type);
        return postStateEntity;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPostState() {
        return postState;
    }

    public void setPostState(int postState) {
        this.postState = postState;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }
}
