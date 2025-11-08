package com.xposed.briaccessibilityservice.utils;

import android.app.Activity;
import android.widget.Toast;

import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;

/**
 * 权限管理工具类
 * 统一管理应用权限申请和检查
 */
public class PermissionManager {
    
    private final Activity activity;
    
    public PermissionManager(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * 检查悬浮窗权限
     */
    public boolean hasOverlayPermission() {
        return XXPermissions.isGrantedPermission(activity, PermissionLists.getSystemAlertWindowPermission());
    }
    
    /**
     * 请求悬浮窗权限
     */
    public void requestOverlayPermission(PermissionCallback callback) {
        if (hasOverlayPermission()) {
            callback.onPermissionGranted();
            return;
        }
        
        XXPermissions.with(activity)
                .permission(PermissionLists.getSystemAlertWindowPermission())
                .request((grantedList, deniedList) -> {
                    if (!grantedList.isEmpty()) {
                        callback.onPermissionGranted();
                    } else {
                        Toast.makeText(activity, "请授予悬浮窗权限", Toast.LENGTH_SHORT).show();
                        callback.onPermissionDenied();
                    }
                });
    }
    
    /**
     * 权限回调接口
     */
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }
}