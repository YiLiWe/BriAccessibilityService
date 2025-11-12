package com.xposed.briaccessibilityservice.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.xposed.briaccessibilityservice.room.entity.PostCollectionErrorEntity;

import java.util.List;

@Dao
public interface PostCollectionErrorDao {
    @Insert
    long insert(PostCollectionErrorEntity postCollectionErrorEntity);

    @Query("DELETE FROM post_collection_error WHERE uid = :id")
    void delete(long id);

    @Query("SELECT * FROM post_collection_error ORDER BY uid DESC LIMIT :limit OFFSET :offset ")
    List<PostCollectionErrorEntity> queryPageVideo(int limit, int offset);
}
