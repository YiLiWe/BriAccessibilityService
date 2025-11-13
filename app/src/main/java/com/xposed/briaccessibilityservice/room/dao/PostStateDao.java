package com.xposed.briaccessibilityservice.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.xposed.briaccessibilityservice.room.entity.BillEntity;
import com.xposed.briaccessibilityservice.room.entity.PostStateEntity;

import java.util.List;

@Dao
public interface PostStateDao {
    @Insert
    void insert(PostStateEntity stateEntity);

    @Query("SELECT count(*) FROM post_state p WHERE p.post_state=:state")
    long countPostState(int state);

    @Query("SELECT count(*) FROM post_state p WHERE p.post_state=:state and p.type=:type")
    long countPostStateAndType(int state,int type);

    @Query("SELECT * FROM post_state ORDER BY uid DESC LIMIT :limit OFFSET :offset ")
    List<PostStateEntity> queryPageVideo(int limit, int offset);

    @Query("SELECT * FROM post_state c where c.post_state=:state LIMIT :limit OFFSET :offset")
    List<PostStateEntity> queryByState(int limit, int offset, int state);

    //根据单词名称修改单词等级
    @Query("UPDATE post_state SET post_state= :state WHERE uid = :id")
    void updateStateById(long id, int state);
}
