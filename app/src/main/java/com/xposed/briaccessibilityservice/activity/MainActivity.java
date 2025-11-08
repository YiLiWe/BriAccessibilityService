package com.xposed.briaccessibilityservice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.xposed.briaccessibilityservice.activity.base.BaseActivity;
import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private AppConfig appConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appConfig = new AppConfig(this);
        appConfig.getAllConfig(binding.cardNumber, binding.collectUrl, binding.payUrl, binding.lockPass, binding.pass);
        initViewClick();
        requestOverlayPermission();
    }

    private void initViewClick() {
        binding.save.setOnClickListener(this::saveClick);
        binding.start.setOnClickListener(this::jumpToSettingPage);
    }


    //点击跳转无障碍
    private void jumpToSettingPage(View context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //点击保存
    private void saveClick(View view) {
        if (!appConfig.setAllConfig(binding.cardNumber, binding.collectUrl, binding.payUrl, binding.lockPass, binding.pass)) {
            Toast.makeText(this, "不能留空", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查悬浮窗权限
     */
    public boolean hasOverlayPermission() {
        return XXPermissions.isGrantedPermission(this, PermissionLists.getSystemAlertWindowPermission());
    }

    /**
     * 请求悬浮窗权限
     */
    public void requestOverlayPermission() {
        if (hasOverlayPermission()) {
            return;
        }
        XXPermissions.with(this)
                .permission(PermissionLists.getSystemAlertWindowPermission())
                .request((grantedList, deniedList) -> {
                    if (grantedList.isEmpty()) {
                        Toast.makeText(this, "请授予悬浮窗权限", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
