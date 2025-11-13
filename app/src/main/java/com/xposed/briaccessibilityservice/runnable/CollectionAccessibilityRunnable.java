package com.xposed.briaccessibilityservice.runnable;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xposed.briaccessibilityservice.runnable.response.CollectBillResponse;
import com.xposed.briaccessibilityservice.runnable.response.ResultResponse;
import com.xposed.briaccessibilityservice.server.PayAccessibilityService;
import com.xposed.briaccessibilityservice.utils.Logs;

import java.io.IOException;

import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

//获取归集
@Getter
public class CollectionAccessibilityRunnable implements Runnable {
    private final PayAccessibilityService service;
    private String cardNumber;
    private String collectUrl;

    public CollectionAccessibilityRunnable(PayAccessibilityService suShellService) {
        this.service = suShellService;
        initData();
    }

    public void initData() {
        SharedPreferences sharedPreferences = service.getSharedPreferences("info", Context.MODE_PRIVATE);
        cardNumber = sharedPreferences.getString("cardNumber", null);
        collectUrl = sharedPreferences.getString("collectUrl", null);
    }

    @Override
    public void run() {
        while (service.isRun()) {
            if (cardNumber == null || collectUrl == null) {
                initData();
                continue;
            }
            if (service.getBalance().isEmpty()) {
                stop();
                continue;
            }
            if (service.getBalance().equals("0")) {
                stop();
                continue;
            }
            if (service.getCollectBillResponse() != null) {
                stop();
                continue;
            }
            if (service.getTakeLatestOrderBean() != null) {
                stop();
                continue;
            }
            CollectBillResponse collectBillResponse = getCollectBean();
            if (collectBillResponse != null) {
                service.getLogWindow().print("归集账单成功:" + collectBillResponse.getId());
                service.setCollectBillResponse(collectBillResponse);
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

    private CollectBillResponse getCollectBean() {
        String getCollectRequest = getCollect();
        if (getCollectRequest == null) return null;
        Logs.d("请求归集结果:" + getCollectRequest);
        ResultResponse response = JSON.to(ResultResponse.class, getCollectRequest);
        if (response != null) {
            if (response.getCode() == 200) {
                if (response.getData() instanceof JSONObject jsonObject) {
                    return jsonObject.to(CollectBillResponse.class);
                }
            }
        }
        return null;
    }

    private String getCollect() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Long money = Long.parseLong(service.getBalance());
        Long moneyA = money / 100;
        Request request = new Request.Builder()
                .url(String.format("%sv1/getCollect?cardNumber=%s&balance=%s", collectUrl, cardNumber, moneyA))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) return null;
                    String text = responseBody.string();
                    return text;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
