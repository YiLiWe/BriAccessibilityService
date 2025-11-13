package com.xposed.briaccessibilityservice.server;

import androidx.annotation.NonNull;

import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.room.dao.PostStateDao;
import com.xposed.briaccessibilityservice.room.entity.PostStateEntity;
import com.xposed.briaccessibilityservice.utils.Logs;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PullPost implements Callback {
    private final PostStateEntity postStateEntity;
    private final AppConfig appConfig;
    private final PostStateDao postStateDao;

    public PullPost(PostStateDao postStateDao, PostStateEntity postStateEntity, AppConfig appConfig) {
        this.postStateEntity = postStateEntity;
        this.appConfig = appConfig;

        this.postStateDao = postStateDao;
    }

    public void start() {
        if (postStateEntity.getType() == 0) {
            payState();
        } else {
            collectState();
        }
    }

    //归集状态提交
    private void collectState() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format("%sv1/collectStatus?id=%s&state=%s&error=%s",
                        appConfig.getCollectUrl(), postStateEntity.getOrderNo(),
                        postStateEntity.getState(), postStateEntity.getError()))
                .build();
        okHttpClient.newCall(request).enqueue(this);
    }

    //代付状态
    private void payState() {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder requestBody = new FormBody.Builder();
        int state = postStateEntity.getState();
        if (state == 1) {
            requestBody.add("paymentCertificate", "Transaction Successful");
            Logs.d(postStateEntity.getOrderNo() + "转账完毕，结果:成功");
        } else {
            Logs.d(postStateEntity.getOrderNo() + "转账完毕，结果:失败 原因:" + postStateEntity.getError());
        }
        requestBody.add("state", String.valueOf(state));
        String timeStr = new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE)
                .format(System.currentTimeMillis());
        requestBody.add("paymentTime", timeStr);
        requestBody.add("failReason", postStateEntity.getError());
        requestBody.add("orderNo", postStateEntity.getOrderNo());
        Request request = new Request.Builder()
                .post(requestBody.build())
                .url(appConfig.getPayUrl() + "app/payoutOrderCallback")
                .build();
        client.newCall(request).enqueue(this);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        postStateEntity.setPostState(0);
        postStateDao.insert(postStateEntity);
        call.clone();

        Logs.d(postStateEntity.getOrderNo() + "二次提交失败:" + e.getMessage());
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        try (ResponseBody responseBody = response.body()) {
            if (responseBody != null) {
                String text = responseBody.string();
                Logs.d(postStateEntity.getOrderNo() + "二次提交:" + text);
            }
        }
        postStateEntity.setPostState(response.isSuccessful() ? 1 : 0);
        postStateDao.insert(postStateEntity);
        response.close();
        call.clone();
    }
}
