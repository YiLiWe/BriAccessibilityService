package com.xposed.briaccessibilityservice.runnable;

import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.room.AppDatabase;
import com.xposed.briaccessibilityservice.room.dao.PostStateDao;
import com.xposed.briaccessibilityservice.room.entity.PostStateEntity;
import com.xposed.briaccessibilityservice.server.PayAccessibilityService;
import com.xposed.briaccessibilityservice.server.PullPostSynchronize;

import java.util.List;

public class PostStateRunnable implements Runnable {
    private final PayAccessibilityService service;
    private final AppConfig appConfig;
    private final PostStateDao postStateDao;

    public PostStateRunnable(PayAccessibilityService service) {
        AppDatabase appDatabase = AppDatabase.getInstance(service);
        this.postStateDao = appDatabase.postStateDao();
        this.service = service;
        this.appConfig = service.getAppConfig();

        service.getLogWindow().printA("添加失败状态数:" + postStateDao.countPostState(0));
    }

    @Override
    public void run() {
        while (service.isRun()) {
            List<PostStateEntity> billEntities = postStateDao.queryByState(10, 0, 0);
            for (PostStateEntity billEntity : billEntities) {
                PullPostSynchronize pullPost = new PullPostSynchronize(postStateDao, billEntity, appConfig);
                pullPost.start();
            }
            stop();
        }
    }

    private void stop() {
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
