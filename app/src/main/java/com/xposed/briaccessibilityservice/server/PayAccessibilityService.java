package com.xposed.briaccessibilityservice.server;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.room.AppDatabase;
import com.xposed.briaccessibilityservice.room.dao.BillDao;
import com.xposed.briaccessibilityservice.room.entity.BillEntity;
import com.xposed.briaccessibilityservice.room.entity.PostStateEntity;
import com.xposed.briaccessibilityservice.runnable.BillRunnable;
import com.xposed.briaccessibilityservice.runnable.CollectionAccessibilityRunnable;
import com.xposed.briaccessibilityservice.runnable.PayRunnable;
import com.xposed.briaccessibilityservice.runnable.PostStateRunnable;
import com.xposed.briaccessibilityservice.runnable.response.CollectBillResponse;
import com.xposed.briaccessibilityservice.runnable.response.TakeLatestOrderBean;
import com.xposed.briaccessibilityservice.server.utils.BillUtils;
import com.xposed.briaccessibilityservice.utils.AccessibleUtil;
import com.xposed.briaccessibilityservice.utils.DeviceUtils;
import com.xposed.briaccessibilityservice.utils.Logs;

import java.util.ArrayList;
import java.util.Date;
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
    private TakeLatestOrderBean takeLatestOrderBean;
    private CollectBillResponse collectBillResponse;
    private PayRunnable payRunnable;
    private BillRunnable billRunnable;
    private PostStateRunnable postStateRunnable;
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
        handler.postDelayed(this::refresh, 10_000);
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

    private void refresh() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> nodeInfos = nodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131366272");
            if (!nodeInfos.isEmpty()) {//账单父类
                if (takeLatestOrderBean == null && collectBillResponse == null) {
                    AccessibleUtil.performPullDown(PayAccessibilityService.this, 500 * 2, 900 * 2, 1000);
                    Logs.d("触发下拉");
                }
            }
        }
        handler.postDelayed(this::refresh, 10_000);
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
            e.printStackTrace();
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
        if (error.startsWith("Sesi telah habis")) return;
        logWindow.printA(takeLatestOrderBean.getOrderNo() + "错误：" + error);
        Logs.d(takeLatestOrderBean.getOrderNo() + "错误:" + error);

        AppDatabase appDatabase = AppDatabase.getInstance(this);
        PostStateEntity postStateEntity = PostStateEntity.create(takeLatestOrderBean.getOrderNo(), 0, 0, error);
        PullPost pullPost = new PullPost(appDatabase.postStateDao(), postStateEntity, appConfig);
        pullPost.start();

        this.takeLatestOrderBean = null;
        balance = "0";
    }

    //转账成功
    private void success(TakeLatestOrderBean takeLatestOrderBean) {
        logWindow.printA(takeLatestOrderBean.getOrderNo() + "转账成功");
        Logs.d(takeLatestOrderBean.getOrderNo() + "转账成功");

        AppDatabase appDatabase = AppDatabase.getInstance(this);
        PostStateEntity postStateEntity = PostStateEntity.create(takeLatestOrderBean.getOrderNo(), 0, 1, "Transaction in Progress");
        PullPost pullPost = new PullPost(appDatabase.postStateDao(), postStateEntity, appConfig);
        pullPost.start();

        this.takeLatestOrderBean = null;
        balance = "0";
    }


    //归集成功
    private void success(CollectBillResponse collectBillResponse) {
        logWindow.printA(collectBillResponse.getId() + "归集成功");
        Logs.d(collectBillResponse.getId() + "归集成功");

        AppDatabase appDatabase = AppDatabase.getInstance(this);
        PostStateEntity postStateEntity = PostStateEntity.create(String.valueOf(collectBillResponse.getId()), 1, 1, "Transaction in Progress");
        PullPost pullPost = new PullPost(appDatabase.postStateDao(), postStateEntity, appConfig);
        pullPost.start();

        this.collectBillResponse = null;
        balance = "0";

    }

    //归集失败
    private void error(String text, CollectBillResponse collectBillResponse) {
        if (text.startsWith("Sesi telah habis")) return;
        logWindow.printA(collectBillResponse.getId() + "归集失败");
        Logs.d(collectBillResponse.getId() + "归集失败");

        AppDatabase appDatabase = AppDatabase.getInstance(this);
        PostStateEntity postStateEntity = PostStateEntity.create(String.valueOf(collectBillResponse.getId()), 1, 2, text);
        PullPost pullPost = new PullPost(appDatabase.postStateDao(), postStateEntity, appConfig);
        pullPost.start();

        this.collectBillResponse = null;
        balance = "0";
    }

    //开始转账
    private void transfer(Map<String, AccessibilityNodeInfo> nodeInfoMap, Map<String, AccessibilityNodeInfo> viewIdResourceMap, AccessibilityNodeInfo nodeInfo) {
        if (takeLatestOrderBean == null && collectBillResponse == null) return;

        //钱包转账
        if (takeLatestOrderBean != null && takeLatestOrderBean.isMoney()) {
            if (nodeInfoMap.containsKey("E Wallet")) {
                AccessibilityNodeInfo wallet = nodeInfoMap.get("E Wallet");
                clickButton(wallet.getParent());
            }

            //选择钱包
            if (nodeInfoMap.containsKey("Pilih E Wallet")) {
                List<AccessibilityNodeInfo> bank = nodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131365007");
                for (AccessibilityNodeInfo nodeInfo1 : bank) {
                    List<AccessibilityNodeInfo> nodeInfos = nodeInfo1.findAccessibilityNodeInfosByText(takeLatestOrderBean.getBankName());
                    if (nodeInfos.isEmpty()) continue;
                    clickButton(nodeInfo1);
                }
            }

            //输入卡号
            if (nodeInfoMap.containsKey("Input Nomor")) {
                if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363077")) {
                    AccessibilityNodeInfo edit = viewIdResourceMap.get("id.co.bri.brimo:id/2131363077");
                    AccessibleUtil.inputTextByAccessibility(edit, takeLatestOrderBean.getCardNumber());
                }
            }

            //确认
            if (nodeInfoMap.containsKey(takeLatestOrderBean.getCardNumber())) {
                clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362140");
            }

            //输入金额
            if (nodeInfoMap.containsKey("Nominal")) {
                if (viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131363155")) {
                    AccessibilityNodeInfo edit = viewIdResourceMap.get("id.co.bri.brimo:id/2131363155");
                    AccessibleUtil.inputTextByAccessibility(edit, String.valueOf(takeLatestOrderBean.getAmount()));
                    clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362282");
                    Logs.d("点击确认输入金额");
                }
            }


            //确认转账
            if (nodeInfoMap.containsKey("Konfirmasi")) {
                clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362129");
            }
        }

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
            if (nodeInfoMap.containsKey(collectBillResponse.getPhone()) && nodeInfoMap.containsKey(collectBillResponse.getBank())) {
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
        }

        //转账成功
        if (nodeInfoMap.containsKey("Transaksi Berhasil") && viewIdResourceMap.containsKey("id.co.bri.brimo:id/2131362212")) {
            if (takeLatestOrderBean != null) {
                success(takeLatestOrderBean);
            } else if (collectBillResponse != null) {
                success(collectBillResponse);
            }
            clickButton(viewIdResourceMap, "id.co.bri.brimo:id/2131362212");
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
            if (billEntity.isNull()) continue;
            if (dao.countByText(billEntity.toString()) > 0) continue;
            if (billEntity.getMoney() == null) continue;
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
                if (takeLatestOrderBean != null) {
                    if (takeLatestOrderBean.isMoney() && nodeInfoMap.containsKey("Top Up")) {//钱包转账
                        List<AccessibilityNodeInfo> item = nodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131365888");
                        item.forEach(accessibilityNodeInfo -> {
                            List<AccessibilityNodeInfo> transfer = accessibilityNodeInfo.findAccessibilityNodeInfosByText("Top Up");
                            if (!transfer.isEmpty()) {//点击转账
                                clickButton(accessibilityNodeInfo);
                            }
                        });
                    } else if (nodeInfoMap.containsKey("Transfer")) {//普通转账
                        List<AccessibilityNodeInfo> item = nodeInfo.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131365888");
                        item.forEach(accessibilityNodeInfo -> {
                            List<AccessibilityNodeInfo> transfer = accessibilityNodeInfo.findAccessibilityNodeInfosByText("Transfer");
                            if (!transfer.isEmpty()) {//点击转账
                                clickButton(accessibilityNodeInfo);
                            }
                        });
                    }
                } else if (nodeInfoMap.containsKey("Transfer")) {
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
                logWindow.printA("重新登录");
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
        postStateRunnable = new PostStateRunnable(this);
        collectionAccessibilityRunnable = new CollectionAccessibilityRunnable(this);
        new Thread(billRunnable).start();
        new Thread(payRunnable).start();
        new Thread(postStateRunnable).start();
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
