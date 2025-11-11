package com.xposed.briaccessibilityservice.server;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.runnable.PayRunnable;
import com.xposed.briaccessibilityservice.runnable.response.TakeLatestOrderBean;
import com.xposed.briaccessibilityservice.server.utils.BillUtils;
import com.xposed.briaccessibilityservice.utils.AccessibleUtil;
import com.xposed.briaccessibilityservice.utils.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PayAccessibilityService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());

    //=========实体类==========
    private LogWindow logWindow;
    private AppConfig appConfig;
    private SuServer suServer;
    private TakeLatestOrderBean takeLatestOrderBean;
    private PayRunnable payRunnable;

    //=========局部变量=========
    private boolean isBill = false;
    private boolean isRun = true;

    private boolean isOk = false;

    //=========局部界面信息=======
    private String balance = "0";

    @Override
    public void onCreate() {
        super.onCreate();
        initNew();
        initRun();
    }

    public synchronized void setTakeLatestOrderBean(TakeLatestOrderBean takeLatestOrderBean) {
        this.takeLatestOrderBean = takeLatestOrderBean;
    }

    private void initRun() {
        // if (!isRun) return;
        handler.postDelayed(this::handlerAccessibility, 2000);
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
            callAccessibility(nodeInfos, nodeInfo);
            initRun();
        }
    }

    private void callAccessibility(List<AccessibilityNodeInfo> nodeInfos, AccessibilityNodeInfo nodeInfo) {
        Map<String, AccessibilityNodeInfo> nodeInfoMap = AccessibleUtil.toTextMap(nodeInfos);
        Map<String, AccessibilityNodeInfo> viewIdResourceMap = AccessibleUtil.toViewIdResourceMap(nodeInfos);
        try {
            login(nodeInfoMap, viewIdResourceMap);
            home(nodeInfoMap, viewIdResourceMap, nodeInfo);
            mutasi(nodeInfoMap, viewIdResourceMap);
            TambahPenerimaBaru(nodeInfoMap, viewIdResourceMap);
            transfer(nodeInfoMap, viewIdResourceMap, nodeInfo);
            backToolbar(nodeInfoMap, viewIdResourceMap, nodeInfo);
        } catch (Throwable e) {
            logWindow.printA("代码执行异常：" + e.getMessage());
            Logs.d("代码执行异常:" + e.getMessage());
        }
    }

    private void backToolbar(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap, AccessibilityNodeInfo nodeInfo) {
        if (takeLatestOrderBean == null) {
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366778")) {
                AccessibilityNodeInfo toolbar = viewIdResourceMap.get("id.co.bri.brimo:id/2131366778");
                AccessibilityNodeInfo back = toolbar.getChild(0);
                clickButton(back);
                Logs.d("控件信息:" + back.toString());
            }
        }
    }

    //开始转账
    private void transfer(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap, AccessibilityNodeInfo nodeInfo) {
        if (takeLatestOrderBean == null) return;

        //点击选择银行编码
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363024")) {
            AccessibilityNodeInfo bank = viewIdResourceMap.get("id.co.bri.brimo:id/2131363024");
            String text = bank.getText().toString();
            if (!text.equals(takeLatestOrderBean.getBankName())) {
                clickButton(bank);
            }
        }

        //判断银行编码存在，输入卡号
        if (nodeInfoMap.containsKey(takeLatestOrderBean.getBankName())) {
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363171")) {//确认银行输入
                AccessibilityNodeInfo edit = viewIdResourceMap.get("id.co.bri.brimo:id/2131363171");
                AccessibleUtil.inputTextByAccessibility(edit, takeLatestOrderBean.getCardNumber());
            }
        }

        //输入银行编码
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366522")) {
            AccessibilityNodeInfo input = viewIdResourceMap.get("id.co.bri.brimo:id/2131366522");
            AccessibleUtil.inputTextByAccessibility(input, takeLatestOrderBean.getBankName());

            List<AccessibilityNodeInfo> banks = nodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368339");
            List<AccessibilityNodeInfo> bankText = new ArrayList<>();
            for (AccessibilityNodeInfo bank : banks) {
                List<AccessibilityNodeInfo> bankTextA = bank.findAccessibilityNodeInfosByText(takeLatestOrderBean.getBankName());
                bankText.addAll(bankTextA);
            }
            for (AccessibilityNodeInfo bank : bankText) {
                String text = bank.getText().toString();
                if (text.equals(takeLatestOrderBean.getBankName())) {
                    clickButton(bank.getParent().getParent());
                }
            }
        }


        if (nodeInfoMap.containsKey(takeLatestOrderBean.getCardNumber()) && nodeInfoMap.containsKey(takeLatestOrderBean.getBankName())) {
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131362256")) {
                Logs.d("点击运行");
                AccessibilityNodeInfo button = viewIdResourceMap.get("id.co.bri.brimo:id/2131362256");
                AccessibleUtil.Click(this, button);
            }
        }

        //是否账号错误
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366944")) {
            AccessibilityNodeInfo error = viewIdResourceMap.get("id.co.bri.brimo:id/2131366944");
            logWindow.printA("错误：" + error.getText().toString());
            takeLatestOrderBean = null;
        }
    }

    private void TambahPenerimaBaru(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        if (takeLatestOrderBean == null) return;
        if (nodeInfoMap.containsKey("Tambah Penerima Baru")) {
            clickButton(nodeInfoMap, "Tambah Penerima Baru");
        }
    }

    //账单
    private void mutasi(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366272")) {//账单父类
            AccessibilityNodeInfo list = viewIdResourceMap.get("id.co.bri.brimo:id/2131366272");
            if (list != null) {
                List<AccessibilityNodeInfo> nodeInfos = list.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131364918");
                BillUtils billEntity = new BillUtils(nodeInfos);
            }
            handler.postDelayed(() -> {
                if (takeLatestOrderBean == null) AccessibleUtil.performPullDown(PayAccessibilityService.this, 500 * 2, 800 * 2, 2000);
            }, 2000);
            isBill = false;

            //前往首页转账
            if (takeLatestOrderBean != null) {
                clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362020");
            }
        }
    }


    //首页
    private void home(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap, AccessibilityNodeInfo nodeInfo) {
        getMoney(nodeInfoMap, viewIdResourceMap);

        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363886")) {
            clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131363886");
        }

        //判断是否首页
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131367227")) {
            if (takeLatestOrderBean != null) {//有订单,点击转账
                if (nodeInfoMap.containsKey("Transfer")) {
                    List<AccessibilityNodeInfo> item = nodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131365888");
                    item.forEach(accessibilityNodeInfo -> {
                        List<AccessibilityNodeInfo> transfer = accessibilityNodeInfo.findAccessibilityNodeInfosByText("Transfer");
                        if (!transfer.isEmpty()) {//点击转账
                            clickButton(accessibilityNodeInfo);
                        }
                    });
                }
            } else if (isBill) {   //首页余额，点击查看账单
                clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362021");
            }
        }
    }

    //获取余额
    private void getMoney(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131367227")) {
            AccessibilityNodeInfo nodeInfo = viewIdResourceMap.get("id.co.bri.brimo:id/2131367227");
            if (nodeInfo != null) {
                String text = nodeInfo.getText().toString();
                String numbersOnly = text.replaceAll("[^0-9]", "");
                if (!numbersOnly.isEmpty()) {
                    this.balance = numbersOnly;
                    Logs.d("余额：" + this.balance);
                }
            }
            isBill = true;
        }
    }

    //登录
    private void login(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        String pass = "Zh112212";
        //点击登录按钮
        if (nodeInfoMap.containsKey("Kontak \n" +
                "Kami")) {
            clickButton(nodeInfoMap, "Login");
        } else if (nodeInfoMap.containsKey("Lupa Username/Password? ")) {//弹窗登录
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363182")) {
                AccessibilityNodeInfo accessibilityNodeInfo = viewIdResourceMap.get("id.co.bri.brimo:id/2131363182");
                AccessibleUtil.inputTextByAccessibility(accessibilityNodeInfo, pass);
            }
        }
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131362361")) {//点击登录
            AccessibilityNodeInfo login = viewIdResourceMap.get("id.co.bri.brimo:id/2131362361");
            if (login.isEnabled()) {
                clickButton(login);
            }
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
        suServer = new SuServer();
        logWindow = new LogWindow(this);
        appConfig = new AppConfig(this);
        if (appConfig.isConfigValid()) {
            logWindow.printA("配置设置不全");
            // isRun = false;
        }
        payRunnable = new PayRunnable(this);
        new Thread(payRunnable).start();
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
