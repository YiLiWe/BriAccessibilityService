package com.xposed.briaccessibilityservice.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

//提交失败账单
@Getter
@Setter
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
    public Date createDate;

    public static PostStateEntity create(String orderNo, int type, int state, String error) {
        PostStateEntity postStateEntity = new PostStateEntity();
        postStateEntity.setState(state);
        postStateEntity.setOrderNo(orderNo);
        postStateEntity.setPostState(0);
        postStateEntity.setError(error);
        postStateEntity.setCreateDate(new Date());
        postStateEntity.setType(type);
        return postStateEntity;
    }
}
