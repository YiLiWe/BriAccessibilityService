package com.xposed.briaccessibilityservice.utils;

import java.util.Calendar;

public class TimeUtils {
    /**
     * 检查当前时间是否在0:00-7:00之间
     * @return true表示在0:00-7:00之间
     */
    public static boolean isNightToMorning() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24小时制
        return hour >= 0 && hour < 7;
    }
}
