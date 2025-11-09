package com.xposed.briaccessibilityservice.server;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.utils.AccessibleUtil;
import com.xposed.briaccessibilityservice.utils.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

public class PayAccessibilityService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());

    //=========实体类==========
    private LogWindow logWindow;
    private AppConfig appConfig;
    private SuServer suServer;

    //=========局部变量=========
    private boolean isRun = true;

    @Override
    public void onCreate() {
        super.onCreate();
        suServer = new SuServer();
        initNew();
        initRun();
    }

    private void initRun() {
        // if (!isRun) return;
        handler.postDelayed(this::handlerAccessibility, 5000);
    }

    //定时启动
    private void handlerAccessibility() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            initRun();
        } else if (nodeInfo.getChildCount() == 0) {
            initRun();
        } else {
            List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
            AccessibleUtil.getAccessibilityNodeInfoS(nodeInfos, nodeInfo);
            callAccessibility(nodeInfos);
            initRun();
        }
    }

    private void callAccessibility(List<AccessibilityNodeInfo> nodeInfos) {
        Map<String, AccessibilityNodeInfo> nodeInfoMap = AccessibleUtil.toTextMap(nodeInfos);
        for (String text : nodeInfoMap.keySet()) {
            Logs.d(text);
        }
        Map<String, AccessibilityNodeInfo> viewIdResourceMap = AccessibleUtil.toViewIdResourceMap(nodeInfos);
        try {
            login(nodeInfoMap, viewIdResourceMap);
            home(nodeInfoMap, viewIdResourceMap);
            mutasi(nodeInfoMap, viewIdResourceMap);
        } catch (Throwable e) {
            logWindow.printA("代码执行异常：" + e.getMessage());
            Logs.d("代码执行异常:" + e.getMessage());
        }
    }

    //账单
    private void mutasi(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366272")) {//账单父类
            AccessibilityNodeInfo list = viewIdResourceMap.get("id.co.bri.brimo:id/2131366272");
            if (list == null) return;
            List<AccessibilityNodeInfo> nodeInfos = list.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131364918");
            BillEntity billEntity = new BillEntity(nodeInfos);
            Logs.d(billEntity.toString());
        }
    }

    @Data
    @ToString
    private class BillEntity {
        private String name; //Transfer Ke RAGA AJAIBAN via BRImo
        private String money;//- Rp15.000,00 + Rp500.000,00
        private String time;//18:28:16 WIB

        public BillEntity(List<AccessibilityNodeInfo> nodeInfos) {
            for (AccessibilityNodeInfo item : nodeInfos) {
                name = handlerName(item.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368916"));
                money = handlerName(item.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368297"));
                time = handlerName(item.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368697"));
            }
        }

        public String handlerName(List<AccessibilityNodeInfo> nodeInfos) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : nodeInfos) {
                return accessibilityNodeInfo.getText().toString();
            }
            return null;
        }
    }

    //首页
    private void home(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        getMoney(nodeInfoMap, viewIdResourceMap);

        //首页余额，点击查看账单
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131367227")) {
            clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362021");
        }
    }

    //获取余额
    private void getMoney(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131367227")) {
            AccessibilityNodeInfo nodeInfo = viewIdResourceMap.get("id.co.bri.brimo:id/2131367227");
            String text = nodeInfo.getText().toString();
            Logs.d("余额：" + text);
        }
    }

    //登录
    private void login(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        //点击登录按钮
        if (nodeInfoMap.containsKey("Kontak \n" +
                "Kami")) {
            clickButton(nodeInfoMap, "Login");


            //输入登录密码
            handler.postDelayed(() -> {
                logWindow.printA("执行输入登录密码:");
                suServer.executeCommand("input tap 550 1780");
                handler.postDelayed(() -> {
                    suServer.executeCommand("input text 'Zh112212'");
                    handler.postDelayed(() -> suServer.executeCommand("input tap 550 1350"), 2000);
                }, 2000);
            }, 2000);

        }
    }

    private void clickButton(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return;
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private void clickButton(Map<String, AccessibilityNodeInfo> nodeInfoMap, String text) {
        if (nodeInfoMap.containsKey(text)) {
            clickButton(nodeInfoMap.get(text));
        }
    }


    private void initNew() {
        logWindow = new LogWindow(this);
        appConfig = new AppConfig(this);
        if (appConfig.isConfigValid()) {
            logWindow.printA("配置设置不全");
            isRun = false;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRun = false;
        logWindow.destroy();
        suServer.closeSession();
    }
}
