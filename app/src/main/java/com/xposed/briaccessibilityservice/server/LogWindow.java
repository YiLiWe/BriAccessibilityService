package com.xposed.briaccessibilityservice.server;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.xposed.briaccessibilityservice.databinding.LayoutLogBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogWindow {
    private final Handler handler;
    private final Service service;
    private final LayoutLogBinding binding;
    private WindowManager windowManager;

    public LogWindow(Service service) {
        handler = new Handler(Looper.getMainLooper());
        this.service = service;
        this.binding = LayoutLogBinding.inflate(LayoutInflater.from(service));
        init();
    }

    public void print(String str) {
        handler.post(() -> printA(str));
    }

    public void printHandler(String str) {
        print(str);
    }


    public void printA(String str) {
        binding.text.append("\n" + getCurrentDate() + ": " + str);
        binding.scroll.post(() -> binding.scroll.fullScroll(View.FOCUS_DOWN));
        String text = binding.text.getText().toString();
        if (text.split("\n").length > 10) {
            binding.text.setText("");
        }
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void init() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dpToPx(service, 300),
                dpToPx(service, 200),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |  // 关键：悬浮窗不获取焦点
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, // 关键：悬浮窗不可触摸（穿透点击）
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(binding.getRoot(), params);
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
        windowManager.removeView(binding.getRoot());
    }

    public int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f); // 四舍五入
    }
}
