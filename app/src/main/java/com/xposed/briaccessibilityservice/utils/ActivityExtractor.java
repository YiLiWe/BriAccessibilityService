package com.xposed.briaccessibilityservice.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity名称提取器
 * 负责从系统输出中提取Activity名称
 */
public class ActivityExtractor {
    
    /**
     * 从系统输出中提取Activity名称
     */
    public static String extractActivityName(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        // 提取第一条日志
        String firstLine = input.split("\n")[0].trim();
        
        // 分割字符串获取Activity全路径
        String[] parts = firstLine.split(" ");
        if (parts.length < 3) {
            return null;
        }
        
        String fullPath = parts[2]; // 得到类似 "bin.mt.plus/bin.mt.plus.MainLightIcon"
        
        Pattern pattern = Pattern.compile("/([^/]+)\\}");
        Matcher matcher = pattern.matcher(fullPath);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}