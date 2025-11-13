package com.xposed.briaccessibilityservice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.xposed.briaccessibilityservice.R;
import com.xposed.briaccessibilityservice.activity.base.BaseActivity;
import com.xposed.briaccessibilityservice.config.AppConfig;
import com.xposed.briaccessibilityservice.databinding.ActivityMainBinding;
import com.xposed.briaccessibilityservice.room.AppDatabase;
import com.xposed.briaccessibilityservice.room.dao.BillDao;
import com.xposed.briaccessibilityservice.room.dao.PostStateDao;
import com.xposed.briaccessibilityservice.utils.DeviceUtils;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private AppConfig appConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.toolbar.setTitle(getString(R.string.app_name) + " V" + DeviceUtils.getVerName(this));

        appConfig = new AppConfig(this);
        appConfig.getAllConfig(binding.cardNumber, binding.collectUrl, binding.payUrl, binding.lockPass, binding.pass);
        initViewClick();
        requestOverlayPermission();

        initData();
    }

    private void initData() {
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        PostStateDao postStateDao = appDatabase.postStateDao();
        BillDao billDao = appDatabase.billDao();

        binding.text.append("\\代付待提交状态数:" + postStateDao.countPostStateAndType(0,0));
        binding.text.append("\n代付总提交成功数:" + postStateDao.countPostStateAndType(1,0));
        binding.text.append("\\归集待提交状态数:" + postStateDao.countPostStateAndType(0,1));
        binding.text.append("\n归集总提交成功数:" + postStateDao.countPostStateAndType(1,1));

        binding.text.append("\n待提交账单数:" + billDao.countPostState(0));
        binding.text.append("\n总提交账单数:" + billDao.countPostState(1));
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
