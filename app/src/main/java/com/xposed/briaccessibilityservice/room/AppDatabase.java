package com.xposed.briaccessibilityservice.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.xposed.briaccessibilityservice.room.dao.BillDao;
import com.xposed.briaccessibilityservice.room.dao.PostStateDao;
import com.xposed.briaccessibilityservice.room.entity.BillEntity;
import com.xposed.briaccessibilityservice.room.entity.PostStateEntity;


@Database(entities = {BillEntity.class, PostStateEntity.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase mAppDatabase;

    public static AppDatabase getInstance(Context context) {
        if (mAppDatabase == null) {
            synchronized (AppDatabase.class) {
                if (mAppDatabase == null) {
                    mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "SqliteData.db")
                            .addMigrations()
                            // 默认不允许在主线程中连接数据库
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return mAppDatabase;
    }

    public abstract BillDao billDao();

    public abstract PostStateDao postStateDao();
}