package com.xposed.briaccessibilityservice.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.xposed.briaccessibilityservice.room.dao.BillDao;
import com.xposed.briaccessibilityservice.room.dao.PostCollectionErrorDao;
import com.xposed.briaccessibilityservice.room.dao.PostPayErrorDao;
import com.xposed.briaccessibilityservice.room.entity.BillEntity;
import com.xposed.briaccessibilityservice.room.entity.PostCollectionErrorEntity;
import com.xposed.briaccessibilityservice.room.entity.PostPayErrorEntity;


@Database(entities = {BillEntity.class, PostCollectionErrorEntity.class, PostPayErrorEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase mAppDatabase;

    public static AppDatabase getInstance(Context context) {
        if (mAppDatabase == null) {
            synchronized (AppDatabase.class) {
                if (mAppDatabase == null) {
                    mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "Bill.db")
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

    public abstract PostCollectionErrorDao postCollectionErrorDao();

    public abstract PostPayErrorDao postPayErrorDao();
}