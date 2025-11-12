package com.xposed.briaccessibilityservice.server;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.room.AppDatabase;
import com.xposed.briaccessibilityservice.room.dao.BillDao;
import com.xposed.briaccessibilityservice.room.entity.BillEntity;
import com.xposed.briaccessibilityservice.runnable.BillRunnable;
import com.xposed.briaccessibilityservice.runnable.CollectionAccessibilityRunnable;
import com.xposed.briaccessibilityservice.runnable.PayRunnable;
import com.xposed.briaccessibilityservice.runnable.response.CollectBillResponse;
import com.xposed.briaccessibilityservice.runnable.response.TakeLatestOrderBean;
import com.xposed.briaccessibilityservice.server.utils.BillUtils;
import com.xposed.briaccessibilityservice.utils.AccessibleUtil;
import com.xposed.briaccessibilityservice.utils.DeviceUtils;
import com.xposed.briaccessibilityservice.utils.Logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Setter
@Getter
public class PayAccessibilityService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());

    //=========实体类==========
    private LogWindow logWindow;
    private AppConfig appConfig;
    private TakeLatestOrderBean takeLatestOrderBean;
    private CollectBillResponse collectBillResponse;
    private PayRunnable payRunnable;
    private BillRunnable billRunnable;
    private CollectionAccessibilityRunnable collectionAccessibilityRunnable;

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
        logWindow.printA("收款(付款)服务V" + DeviceUtils.getVerName(this));
    }

    public synchronized void setTakeLatestOrderBean(TakeLatestOrderBean takeLatestOrderBean) {
        this.takeLatestOrderBean = takeLatestOrderBean;
    }

    public synchronized void setCollectBillResponse(CollectBillResponse collectBillResponse) {
        this.collectBillResponse = collectBillResponse;
    }

    private void initRun() {
        if (!isRun) return;
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
            Logs.d("代码执行异常:" + e.getMessage());
        }
    }

    private void backToolbar(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap, AccessibilityNodeInfo nodeInfo) {
        if (takeLatestOrderBean == null && collectBillResponse == null) {
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366895")) {
                AccessibilityNodeInfo toolbar = viewIdResourceMap.get("id.co.bri.brimo:id/2131366895");
                AccessibilityNodeInfo back = toolbar.getParent().getChild(0);
                clickButton(back);
            }
        }
    }

    //转账失败
    private void error(String error, TakeLatestOrderBean takeLatestOrderBean) {
        logWindow.printA("错误：" + error);
        Logs.d("错误:" + error);
        PullPost(0, error, takeLatestOrderBean);
        this.takeLatestOrderBean = null;
    }

    //转账成功
    private void success(TakeLatestOrderBean takeLatestOrderBean) {
        logWindow.printA(takeLatestOrderBean.getOrderNo() + "转账成功");
        Logs.d(takeLatestOrderBean.getOrderNo() + "转账成功");
        PullPost(1, "Transaction in Progress", takeLatestOrderBean);
        this.takeLatestOrderBean = null;
    }


    //归集成功
    private void success(CollectBillResponse collectBillResponse) {
        postCollectStatus(1, "归集成功", collectBillResponse.getId());
        setCollectBillResponse(null);
        balance = "0";
        logWindow.printA("归集成功");
        Logs.d("归集成功");
    }

    //归集失败
    private void error(String text, CollectBillResponse collectBillResponse) {
        postCollectStatus(2, text, collectBillResponse.getId());
        setCollectBillResponse(null);
        balance = "0";
        Logs.d("转账失败");
        logWindow.printA("归集失败");
    }


    private void postCollectStatus(int state, String error, long id) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format("%sv1/collectStatus?id=%s&state=%s&error=%s", appConfig.getCollectUrl(), id, state, error))
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.clone();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                response.close();
                response.close();
            }
        });
    }


    public void PullPost(int state, String error, TakeLatestOrderBean transferBean) {
        if (transferBean == null) return;
        FormBody.Builder requestBody = new FormBody.Builder();
        if (error.equals("Transaction in Progress")) {
            state = 1;
        }
        if (state == 1) {
            requestBody.add("paymentCertificate", "Transaction Successful");
            Logs.d(transferBean.getOrderNo() + "转账完毕，结果:成功");
        } else {
            Logs.d(transferBean.getOrderNo() + "转账完毕，结果:失败 原因:" + error);
        }
        requestBody.add("state", String.valueOf(state));
        String timeStr = new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(System.currentTimeMillis());
        requestBody.add("paymentTime", timeStr);
        requestBody.add("failReason", error);
        requestBody.add("amount", String.valueOf(transferBean.getAmount()));
        requestBody.add("orderNo", transferBean.getOrderNo());
        Request request = new Request.Builder()
                .post(requestBody.build())
                .url(appConfig.getPayUrl() + "app/payoutOrderCallback")
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.clone();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                response.close();
                call.clone();
            }
        });
    }


    //开始转账
    private void transfer(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap, AccessibilityNodeInfo nodeInfo) {
        if (takeLatestOrderBean == null && collectBillResponse == null) return;

        //点击选择银行编码
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363024")) {
            AccessibilityNodeInfo bank = viewIdResourceMap.get("id.co.bri.brimo:id/2131363024");
            String text = bank.getText().toString();
            if (takeLatestOrderBean != null) {
                if (!text.equals(takeLatestOrderBean.getBankName())) {
                    clickButton(bank);
                }
            } else if (collectBillResponse != null) {
                if (!text.equals(collectBillResponse.getBank())) {
                    clickButton(bank);
                }
            }
        }

        //判断银行编码存在，输入卡号
        if (takeLatestOrderBean != null) {
            if (nodeInfoMap.containsKey(takeLatestOrderBean.getBankName())) {
                if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363171")) {//确认银行输入
                    AccessibilityNodeInfo edit = viewIdResourceMap.get("id.co.bri.brimo:id/2131363171");
                    AccessibleUtil.inputTextByAccessibility(edit, takeLatestOrderBean.getCardNumber());
                }
            }
        } else if (collectBillResponse != null) {
            if (nodeInfoMap.containsKey(collectBillResponse.getBank())) {
                if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363171")) {//确认银行输入
                    AccessibilityNodeInfo edit = viewIdResourceMap.get("id.co.bri.brimo:id/2131363171");
                    AccessibleUtil.inputTextByAccessibility(edit, collectBillResponse.getPhone());
                }
            }
        }

        //输入银行编码
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366522")) {
            AccessibilityNodeInfo input = viewIdResourceMap.get("id.co.bri.brimo:id/2131366522");
            if (takeLatestOrderBean != null) {
                AccessibleUtil.inputTextByAccessibility(input, takeLatestOrderBean.getBankName());
            } else if (collectBillResponse != null) {
                AccessibleUtil.inputTextByAccessibility(input, collectBillResponse.getBank());
            }
            List<AccessibilityNodeInfo> banks = nodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368339");
            List<AccessibilityNodeInfo> bankText = new ArrayList<>();
            for (AccessibilityNodeInfo bank : banks) {
                if (takeLatestOrderBean != null) {
                    List<AccessibilityNodeInfo> bankTextA = bank.findAccessibilityNodeInfosByText(takeLatestOrderBean.getBankName());
                    bankText.addAll(bankTextA);
                } else if (collectBillResponse != null) {
                    List<AccessibilityNodeInfo> bankTextA = bank.findAccessibilityNodeInfosByText(collectBillResponse.getBank());
                    bankText.addAll(bankTextA);
                }
            }
            for (AccessibilityNodeInfo bank : bankText) {
                String text = bank.getText().toString();
                if (takeLatestOrderBean != null) {
                    if (text.equals(takeLatestOrderBean.getBankName())) {
                        clickButton(bank.getParent().getParent());
                    }
                } else if (collectBillResponse != null) {
                    if (text.equals(collectBillResponse.getBank())) {
                        clickButton(bank.getParent().getParent());
                    }
                }
            }
        }

        //输入信息确认转账
        if (takeLatestOrderBean != null) {
            if (nodeInfoMap.containsKey(takeLatestOrderBean.getCardNumber()) && nodeInfoMap.containsKey(takeLatestOrderBean.getBankName())) {
                if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131362256")) {
                    AccessibilityNodeInfo button = viewIdResourceMap.get("id.co.bri.brimo:id/2131362256");
                    AccessibleUtil.ClickX200(this, button);
                }
            }
        } else if (collectBillResponse != null) {
            if (nodeInfoMap.containsKey(collectBillResponse.getCardNumber()) && nodeInfoMap.containsKey(collectBillResponse.getBank())) {
                if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131362256")) {
                    AccessibilityNodeInfo button = viewIdResourceMap.get("id.co.bri.brimo:id/2131362256");
                    AccessibleUtil.ClickX200(this, button);
                }
            }
        }

        //是否账号错误
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131366944")) {
            AccessibilityNodeInfo error = viewIdResourceMap.get("id.co.bri.brimo:id/2131366944");
            String text = error.getText().toString();
            if (takeLatestOrderBean != null) {
                error(text, takeLatestOrderBean);
            } else if (collectBillResponse != null) {
                error(text, collectBillResponse);
            }
            return;
        }


        //转账界面
        if (nodeInfoMap.containsKey("Masukkan Nominal")) {
            //输入金额
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363155")) {
                AccessibilityNodeInfo money = viewIdResourceMap.get("id.co.bri.brimo:id/2131363155");
                String text = money.getText().toString();
                if (text.equals("0")) {
                    if (takeLatestOrderBean != null) {
                        AccessibleUtil.inputTextByAccessibility(money, String.valueOf(takeLatestOrderBean.getAmount()));
                    } else if (collectBillResponse != null) {
                        AccessibleUtil.inputTextByAccessibility(money, String.valueOf(collectBillResponse.getIdPlgn()));
                    }
                } else {
                    if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131362256")) {
                        AccessibilityNodeInfo button = viewIdResourceMap.get("id.co.bri.brimo:id/2131362256");
                        Logs.d("确认转账:" + button.toString());
                        AccessibleUtil.ClickX200(this, button);
                    }
                }
            }

            //错误信息
            if (nodeInfoMap.containsKey("Saldo Anda tidak cukup")) {
                if (takeLatestOrderBean != null) {
                    error("Saldo Anda tidak cukup", takeLatestOrderBean);
                } else if (collectBillResponse != null) {
                    error("Saldo Anda tidak cukup", collectBillResponse);
                }
                return;
            }

        }

        //确认账单
        if (nodeInfoMap.containsKey("Konfirmasi")) {
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131362129")) {
                AccessibilityNodeInfo button = viewIdResourceMap.get("id.co.bri.brimo:id/2131362129");
                clickButton(button);
            }
        }

        //输入支付密码
        if (nodeInfoMap.containsKey("PIN")) {
            String pinPass = appConfig.getLockPass();
            for (int i = 0; i < pinPass.length(); i++) {
                char c = pinPass.charAt(i);
                String charAsString = String.valueOf(c); // 将 char 转为 String
                if (!nodeInfoMap.containsKey(charAsString)) continue;
                AccessibilityNodeInfo nodeInfo1 = nodeInfoMap.get(charAsString);
                clickButton(nodeInfo1);
            }
            if (takeLatestOrderBean != null) {
                success(takeLatestOrderBean);
            } else if (collectBillResponse != null) {
                success(collectBillResponse);
            }
        }
    }


    private void TambahPenerimaBaru(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap) {
        if (takeLatestOrderBean == null && collectBillResponse == null) return;
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
                handlerData(billEntity);
            }
            handler.postDelayed(() -> {
                if (takeLatestOrderBean == null && collectBillResponse == null) {
                    AccessibleUtil.performPullDown(PayAccessibilityService.this, 500 * 2, 800 * 2, 2000);
                }
            }, 2000);
            isBill = false;

            //前往首页转账
            if (takeLatestOrderBean != null) {
                clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362020");
            } else if (collectBillResponse != null) {
                clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362020");
            }

        }
    }


    //处理账单
    private void handlerData(BillUtils billUtils) {
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        BillDao dao = appDatabase.billDao();
        List<BillEntity> billEntities = new ArrayList<>();
        for (BillUtils.BillEntity billEntity : billUtils.getBillEntities()) {
            if (dao.countByText(billEntity.toString()) > 0) continue;
            if (billEntity.getMoney().contains("+")) {
                BillEntity bill = new BillEntity();
                bill.setState(0);
                bill.setMoney(billEntity.getMoney());
                bill.setName(billEntity.getName());
                bill.setText(billEntity.toString());
                bill.setTime(getSystemFormattedTime());
                billEntities.add(bill);
            }
        }
        dao.insert(billEntities);
    }

    /**
     * 使用Android系统提供的格式化工具
     */
    private String getSystemFormattedTime() {
        // 根据系统设置自动适配12/24小时制
        return DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString();
    }


    //首页
    private void home(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap, AccessibilityNodeInfo nodeInfo) {
        getMoney(nodeInfoMap, viewIdResourceMap);

        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363886")) {
            clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131363886");
        }

        //判断是否首页
        if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131367227")) {
            if (takeLatestOrderBean != null || collectBillResponse != null) {//有订单,点击转账
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
        //点击登录按钮
        if (nodeInfoMap.containsKey("Kontak \n" +
                "Kami")) {
            clickButton(nodeInfoMap, "Login");
        } else if (nodeInfoMap.containsKey("Lupa Username/Password? ")) {//弹窗登录
            if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363182")) {
                AccessibilityNodeInfo accessibilityNodeInfo = viewIdResourceMap.get("id.co.bri.brimo:id/2131363182");
                AccessibleUtil.inputTextByAccessibility(accessibilityNodeInfo, appConfig.getPASS());
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
        logWindow = new LogWindow(this);
        appConfig = new AppConfig(this);
        if (appConfig.isConfigValid()) {
            logWindow.printA("配置设置不全");
            isRun = false;
        }
        payRunnable = new PayRunnable(this);
        billRunnable = new BillRunnable(this);
        collectionAccessibilityRunnable = new CollectionAccessibilityRunnable(this);
        new Thread(billRunnable).start();
        new Thread(payRunnable).start();
        new Thread(collectionAccessibilityRunnable).start();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        handlerError();
    }

    private void handlerError() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) return;
        List<AccessibilityNodeInfo> errors = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131366594");
        if (errors.isEmpty()) return;
        for (AccessibilityNodeInfo error : errors) {
            String text = error.getText().toString();
            if (takeLatestOrderBean != null) {
                error(text, takeLatestOrderBean);
            } else if (collectBillResponse != null) {
                error(text, collectBillResponse);
            }
            break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRun = false;
        logWindow.destroy();
    }
}
