package com.xposed.briaccessibilityservice.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AccessibleUtil {

    /**
     * 模拟从上往下拉手势
     * @param service AccessibilityService实例
     * @param startY 起始Y坐标（顶部位置）
     * @param endY 结束Y坐标（底部位置）
     * @param duration 动画持续时间(毫秒)
     */
    public static void performPullDown(AccessibilityService service, int startY, int endY, int duration) {
        // 参数校验
        if (service == null || startY >= endY) {
            return;
        }

        // 创建手势路径（从上往下滑动）
        Path path = new Path();
        int centerX = getScreenCenterX(service);
        path.moveTo(centerX, startY);    // 起点（上方）
        path.lineTo(centerX, endY);      // 终点（下方）

        // 构建手势描述
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(
                path,
                0,      // 立即开始
                duration // 持续时间
        ));

        // 执行手势
        service.dispatchGesture(builder.build(), null, null);
    }


    // 获取屏幕中心X坐标
    public static int getScreenCenterX(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels / 2;
    }


    //获取开头
    public static AccessibilityNodeInfo toStateContentDescMap(Map<String, AccessibilityNodeInfo> nodeInfoMap, String text) {
        for (String key : nodeInfoMap.keySet()) {
            if (key.startsWith(text)) {
                return nodeInfoMap.get(key);
            }
        }
        return null;
    }

    public static Map<String, AccessibilityNodeInfo> toStateContentDescMapX(Map<String, AccessibilityNodeInfo> nodeInfoMap, String text) {
        Map<String, AccessibilityNodeInfo> map = new HashMap<>();
        for (String key : nodeInfoMap.keySet()) {
            if (key.startsWith(text)) {
                map.put(key, nodeInfoMap.get(key));
            }
        }
        return map;
    }


    //获取备注的文字
    public static Map<String, AccessibilityNodeInfo> toContentDescMap(AccessibilityNodeInfo accessibilityNodeInfo) {
        List<AccessibilityNodeInfo> accessibilityNodeInfos = new ArrayList<>();
        AccessibleUtil.getAccessibilityNodeInfoS(accessibilityNodeInfos, accessibilityNodeInfo);
        return toContentDescMap(accessibilityNodeInfos);
    }

    //获取备注的文字
    public static Map<String, AccessibilityNodeInfo> toTextMap(AccessibilityNodeInfo accessibilityNodeInfo) {
        List<AccessibilityNodeInfo> accessibilityNodeInfos = new ArrayList<>();
        AccessibleUtil.getAccessibilityNodeInfoS(accessibilityNodeInfos, accessibilityNodeInfo);
        return toTextMap(accessibilityNodeInfos);
    }

    public static Map<String, AccessibilityNodeInfo> toTextMap(List<AccessibilityNodeInfo> accessibilityNodeInfos) {
        Map<String, AccessibilityNodeInfo> map = new HashMap<>();
        for (AccessibilityNodeInfo node : accessibilityNodeInfos) {
            if (node.getText() != null) {
                map.put(node.getText().toString(), node);
            }
        }
        return map;
    }

    public static Map<String, AccessibilityNodeInfo> toViewIdResourceMap(List<AccessibilityNodeInfo> accessibilityNodeInfos) {
        Map<String, AccessibilityNodeInfo> map = new HashMap<>();
        for (AccessibilityNodeInfo node : accessibilityNodeInfos) {
            if (node.getViewIdResourceName() != null) {
                map.put(node.getViewIdResourceName(), node);
            }
        }
        return map;
    }


    public static Map<String, AccessibilityNodeInfo> toContentDescMap(List<AccessibilityNodeInfo> accessibilityNodeInfos) {
        Map<String, AccessibilityNodeInfo> map = new HashMap<>();
        for (AccessibilityNodeInfo node : accessibilityNodeInfos) {
            if (node.getContentDescription() != null) {
                map.put(node.getContentDescription().toString(), node);
            }
        }
        return map;
    }


    /**
     * 使用无障碍服务进行文本输入（最稳定）
     */
    public static boolean inputTextByAccessibility(AccessibilityNodeInfo targetNode, String text) {
        // 设置新文本
        Bundle setTextArgs = new Bundle();
        setTextArgs.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, setTextArgs);
    }

    public static void getAccessibilityNodeInfoS(List<AccessibilityNodeInfo> nodeInfos, AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo accessibilityNodeInfo = node.getChild(i);
            if (accessibilityNodeInfo != null) {
                nodeInfos.add(accessibilityNodeInfo);
                getAccessibilityNodeInfoS(nodeInfos, accessibilityNodeInfo);
            }
        }
    }

    public static AccessibilityNodeInfo findAccessibilityNodeInfosByViewId(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> switch_widget = nodeInfo.findAccessibilityNodeInfosByViewId(text);
        if (switch_widget.size() == 0) {
            return null;
        }
        for (AccessibilityNodeInfo nodeInfo1 : switch_widget) {
            if (nodeInfo1.getViewIdResourceName().equals(text)) {
                return nodeInfo1;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findAccessibilityNodeInfosByViewIdEnd(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> switch_widget = nodeInfo.findAccessibilityNodeInfosByViewId(text);
        if (switch_widget.size() == 0) {
            return null;
        }
        for (AccessibilityNodeInfo nodeInfo1 : switch_widget) {
            if (nodeInfo1.getViewIdResourceName().endsWith(text)) {
                return nodeInfo1;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findAccessibilityNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> switch_widget = nodeInfo.findAccessibilityNodeInfosByViewId(text);
        if (switch_widget.size() == 0) {
            return null;
        }
        for (AccessibilityNodeInfo nodeInfo1 : switch_widget) {
            if (nodeInfo1.getText().equals(text)) {
                return nodeInfo1;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findAccessibilityNodeInfosByTextS(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> accessibilityNodeInfos = new ArrayList<>();
        AccessibleUtil.getAccessibilityNodeInfoS(accessibilityNodeInfos, nodeInfo);
        for (AccessibilityNodeInfo nodeInfo1 : accessibilityNodeInfos) {
            CharSequence sequence = nodeInfo1.getText();
            if (sequence != null && sequence.toString().contains(text)) {
                return nodeInfo1;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo getScrollableNode(AccessibilityNodeInfo node) {
        List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        AccessibleUtil.getAccessibilityNodeInfoS(nodeInfos, node);
        for (AccessibilityNodeInfo childNode : nodeInfos) {
            if (childNode.isScrollable()) {
                return childNode;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo getClassName(AccessibilityNodeInfo nodeInfo, String className) {
        List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        AccessibleUtil.getAccessibilityNodeInfoS(nodeInfos, nodeInfo);
        for (AccessibilityNodeInfo nodeInfo1 : nodeInfos) {
            if (nodeInfo1.getClassName().equals(className)) {
                return nodeInfo1;
            }
        }
        return null;
    }


    public static void Scrollable(AccessibilityNodeInfo nodeInfo) {
        AccessibilityNodeInfo nodeInfo1 = AccessibleUtil.getScrollableNode(nodeInfo);
        if (nodeInfo1 != null) {
            nodeInfo1.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
    }

    public static void ClickText(AccessibilityService accessibilityService, AccessibilityNodeInfo targetNode, String text) {
        AccessibilityNodeInfo allow = AccessibleUtil.findAccessibilityNodeInfosByText(targetNode, text);//同意授权
        if (allow != null) {
            AccessibleUtil.Click(accessibilityService, allow);
        }
    }

    public static void ClickId(AccessibilityService accessibilityService, AccessibilityNodeInfo targetNode, String id) {
        AccessibilityNodeInfo allow = AccessibleUtil.findAccessibilityNodeInfosByViewId(targetNode, id);//同意授权
        if (allow != null) {
            AccessibleUtil.Click(accessibilityService, allow);
        }
    }

    public static AccessibilityNodeInfo getSwitchWidget(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        getAccessibilityNodeInfoS(nodeInfos, nodeInfo);
        for (AccessibilityNodeInfo nodeInfo1 : nodeInfos) {
            CharSequence ResourceName = nodeInfo1.getViewIdResourceName();
            if (ResourceName != null && ResourceName.toString().equals("android:id/switch_widget")) {
                return nodeInfo1;
            }
        }
        return null;
    }

    public static List<AccessibilityNodeInfo> getWidget(AccessibilityNodeInfo nodeInfo, String className) {
        List<AccessibilityNodeInfo> widget = new ArrayList<>();
        List<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        getAccessibilityNodeInfoS(nodeInfos, nodeInfo);
        for (AccessibilityNodeInfo nodeInfo1 : nodeInfos) {
            CharSequence charSequence = nodeInfo1.getClassName();
            if (charSequence != null && charSequence.equals(className)) {
                widget.add(nodeInfo1);
            }
        }
        return widget;
    }

    public static void Click(AccessibilityService accessibilityService, AccessibilityNodeInfo targetNode) {
        if (targetNode == null) {
            return;
        }
        Rect bounds = new Rect();
        targetNode.getBoundsInScreen(bounds);
        int x = bounds.centerX();
        int y = bounds.centerY();
        Logs.d("点击坐标:"+x+" Y:"+y);
        AccessibleUtil.simulateRealisticClick(accessibilityService, x, y);
    }


    public static void simulateClick(AccessibilityService service, float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100)).build();
        service.dispatchGesture(gestureDescription, null, null);
    }

    public static void simulateRealisticClick(AccessibilityService service, float x, float y) {
        // 1. 添加随机偏移（±5像素）
        Random random = new Random();
        float offsetX = random.nextFloat() * 10 - 5; // [-5, 5]
        float offsetY = random.nextFloat() * 10 - 5; // [-5, 5]
        Path path = new Path();
        path.moveTo(x + offsetX, y + offsetY);

        // 2. 随机化点击时长（50-200ms）
        int duration = 50 + random.nextInt(150);

        // 3. 构建手势
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, duration))
                .build();

        // 4. 分发手势
        service.dispatchGesture(gestureDescription, null, null);
    }



    private static boolean click(AccessibilityNodeInfo it) {
        return it.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

}
