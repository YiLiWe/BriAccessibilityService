package com.xposed.briaccessibilityservice.runnable;


import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.room.AppDatabase;
import com.xposed.briaccessibilityservice.room.dao.BillDao;
import com.xposed.briaccessibilityservice.room.entity.BillEntity;
import com.xposed.briaccessibilityservice.server.PayAccessibilityService;
import com.xposed.briaccessibilityservice.utils.Logs;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

//上传代收
public class BillRunnable implements Runnable {
    private final PayAccessibilityService accessibilityService;
    private final AppConfig appConfig;

    public BillRunnable(PayAccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
        appConfig = accessibilityService.getAppConfig();
    }

    @Override
    public void run() {
        AppDatabase appDatabase = AppDatabase.getInstance(accessibilityService);
        while (accessibilityService.isRun()) {
            BillDao billDao = appDatabase.billDao();
            List<BillEntity> billEntities = billDao.queryByState(10, 0, 0);
            for (BillEntity billEntity : billEntities) {
                postBill(billDao, billEntity);
            }
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void postBill(BillDao billDao, BillEntity billEntity) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String text = String.format("%sv1/briBill?cardNumber=%s&text=%s&money=" + billEntity.getMoney() + "&time=" + billEntity.getTime(),
                appConfig.getCollectUrl(), appConfig.getCardNumber(), billEntity.getText());
        Logs.d("提交内容:" + text);
        Request request = new Request.Builder()
                .url(text)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            Logs.d("响应码:" + response.code());
            if (response.isSuccessful()) {
                billDao.updateStateById(billEntity.getUid(), 1);
            } else {
                billDao.updateStateById(billEntity.getUid(), 0);
            }
            try (ResponseBody body = response.body()) {
                if (body == null) return;
                String result = body.string();
                Logs.d(result);
            }
        } catch (IOException e) {
            billDao.updateStateById(billEntity.getUid(), 0);
            e.printStackTrace();
        }
    }
}
