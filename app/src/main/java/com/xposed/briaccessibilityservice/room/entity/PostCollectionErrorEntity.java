package com.xposed.briaccessibilityservice.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//提交失败的归集状态
@Entity(tableName = "post_collection_error")
public class PostCollectionErrorEntity {
    // 主键
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public long uid;


    // 归集ID
    @ColumnInfo(name = "id")
    public long id;

    //归集状态
    @ColumnInfo(name = "state")
    private int state;

    //失败原因
    @ColumnInfo(name = "error")
    private String error;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
