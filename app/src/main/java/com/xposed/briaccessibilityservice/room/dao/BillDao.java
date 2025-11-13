package com.xposed.briaccessibilityservice.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import com.xposed.briaccessibilityservice.room.entity.BillEntity;

import java.util.List;

@Dao
public interface BillDao {
    @Insert
    long insert(BillEntity bill);

    @Query("SELECT count(*) FROM bill p WHERE p.state=:state")
    long countPostState(int state);

    @Insert
    void insert(List<BillEntity> bill);

    @Query("SELECT COUNT(*) FROM bill WHERE text = :text")
    long countByText(String text);

    @Query("SELECT * FROM bill ORDER BY uid DESC LIMIT :limit OFFSET :offset ")
    List<BillEntity> queryPageVideo(int limit, int offset);

    @Query("SELECT * FROM bill where state=:state LIMIT :limit OFFSET :offset")
    List<BillEntity> queryByState(int limit, int offset, int state);

    //根据单词名称修改单词等级
    @Query("UPDATE bill SET state= :state WHERE uid = :id")
    void updateStateById(long id, int state);

    @Query("DELETE FROM bill")
    void deleteAll();
}
