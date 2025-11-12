package com.xposed.briaccessibilityservice.runnable;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSON;
import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.runnable.response.MessageBean;
import com.xposed.briaccessibilityservice.runnable.response.TakeLatestOrderBean;
import com.xposed.briaccessibilityservice.server.PayAccessibilityService;
import com.xposed.briaccessibilityservice.utils.BankUtils;
import com.xposed.briaccessibilityservice.utils.Logs;
import com.xposed.briaccessibilityservice.utils.TimeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Getter
public class PayRunnable implements Runnable {
    private final PayAccessibilityService service;
    private final AppConfig appConfig;
    private final List<String> Banks = List.of("Gopay Customer", "OVO", "ShopeePay", "DANA", "LinkAja");
    private final Map<String, String> Banks2 = new HashMap<>() {
        {
            put("OVO", "OVO");
            put("LinkAja", "LinkAja");
            put("ShopeePay", "ShopeePay");
            put("Gopay Customer", "GoPay");
            put("DANA", "DANA");
        }
    };

    public PayRunnable(PayAccessibilityService suShellService) {
        this.service = suShellService;
        appConfig = suShellService.getAppConfig();
    }

    @Override
    public void run() {
        while (service.isRun()) {
            if (service.getTakeLatestOrderBean() != null) {
                stop();
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
            TakeLatestOrderBean takeLatestOrderBean = getOrder();
            if (takeLatestOrderBean != null) {
                service.getLogWindow().print("获取到订单:" + takeLatestOrderBean.getOrderNo());
                service.setOk(false);
                service.setTakeLatestOrderBean(takeLatestOrderBean);
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

    public TakeLatestOrderBean getOrder() {
        String text = takeLatestPayoutOrder();
        if (text == null) return null;
        Logs.d("代付订单:" + text);
        if (!text.startsWith("{")) return null;
        MessageBean messageBean = JSON.to(MessageBean.class, text);
        if (messageBean == null) return null;
        if (messageBean.getData() == null) return null;
        TakeLatestOrderBean takeLatestOrderBean1 = messageBean.getData().to(TakeLatestOrderBean.class);
        if (Banks.contains(takeLatestOrderBean1.getBankName())) {
            takeLatestOrderBean1.setMoney(true);
            takeLatestOrderBean1.setBankName(getBank(takeLatestOrderBean1.getBankName()));
        } else {
            takeLatestOrderBean1.setMoney(false);
        }
        return takeLatestOrderBean1;
    }

    private String getBank(String key) {
        String value = Banks2.get(key);
        if (value == null) return key;
        return value;
    }


    //获取代付订单
    public String takeLatestPayoutOrder() {
        RequestBody requestBody = new FormBody.Builder()
                .add("cardNumber", appConfig.getCardNumber())
                .add("balance", service.getBalance())
                .build();
        Request request = new Request.Builder()
                .post(requestBody)
                .url(appConfig.getPayUrl() + "app/takeLatestPayoutOrder")
                .build();
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(request).execute()) {
            try (ResponseBody responseBody = response.body()) {
                if (responseBody != null) {
                    return responseBody.string();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
