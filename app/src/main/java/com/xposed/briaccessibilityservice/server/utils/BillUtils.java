package com.xposed.briaccessibilityservice.server.utils;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BillUtils {
    private final List<BillEntity> billEntities = new ArrayList<>();

    @Data
    public static class BillEntity {
        private String name; //Transfer Ke RAGA AJAIBAN via BRImo
        private String money;//- Rp15.000,00 + Rp500.000,00
        private String time;//18:28:16 WIB

        public BillEntity(AccessibilityNodeInfo item) {
            name = handlerName(item.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368916"));
            money = handlerName(item.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368297"));
            time = handlerName(item.findAccessibilityNodeInfosByViewId("id.co.bri.brimo:id/2131368697"));
        }

        public String handlerName(List<AccessibilityNodeInfo> nodeInfos) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : nodeInfos) {
                return accessibilityNodeInfo.getText().toString();
            }
            return null;
        }

        public String toString() {
            return name + money + time;
        }
    }

    public BillUtils(List<AccessibilityNodeInfo> nodeInfos) {
        for (AccessibilityNodeInfo item : nodeInfos) {
            billEntities.add(new BillEntity(item));
        }
    }
}