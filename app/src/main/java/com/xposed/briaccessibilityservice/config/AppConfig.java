package com.xposed.briaccessibilityservice.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;

import com.google.android.material.textfield.TextInputEditText;

/**
 * 应用配置管理类
 * 统一管理所有配置项的读写操作
 */
public class AppConfig {

    private static final String PREF_NAME = "info";
    private static final String KEY_CARD_NUMBER = "cardNumber";
    private static final String KEY_COLLECT_URL = "collectUrl";
    private static final String KEY_PAY_URL = "payUrl";
    private static final String LOCK_PASS = "lockPass";
    private static final String PASS = "pass";

    private final SharedPreferences sharedPreferences;

    public AppConfig(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ========== Getter方法 ==========

    public String getCardNumber() {
        return sharedPreferences.getString(KEY_CARD_NUMBER, "");
    }

    public String getCollectUrl() {
        return sharedPreferences.getString(KEY_COLLECT_URL, "");
    }

    public String getPayUrl() {
        return sharedPreferences.getString(KEY_PAY_URL, "");
    }

    public String getLockPass() {
        return sharedPreferences.getString(LOCK_PASS, "");
    }

    public String getPASS() {
        return sharedPreferences.getString(PASS, "");
    }

    // ========== Setter方法 ==========

    public void setLockPass(String lockPass) {
        sharedPreferences.edit().putString(LOCK_PASS, lockPass).apply();
    }

    public void setPass(String pass) {
        sharedPreferences.edit().putString(PASS, pass).apply();
    }

    public void setCardNumber(String cardNumber) {
        sharedPreferences.edit().putString(KEY_CARD_NUMBER, cardNumber).apply();
    }

    public void setCollectUrl(String collectUrl) {
        sharedPreferences.edit().putString(KEY_COLLECT_URL, collectUrl).apply();
    }

    public void setPayUrl(String payUrl) {
        sharedPreferences.edit().putString(KEY_PAY_URL, payUrl).apply();
    }

    public boolean isEditable(Editable... editables) {
        for (Editable editable : editables) {
            if (editable == null) return false;
            String text = editable.toString();
            if (text.isEmpty()) return false;
        }
        return true;
    }


    public boolean isConfigValid() {
        String cardNumber = getCardNumber();
        String collectUrl = getCollectUrl();
        String payUrl = getPayUrl();
        String lockPass = getLockPass();
        String passStr = getPASS();
        return isEmpty(cardNumber, collectUrl, payUrl, lockPass, passStr);
    }

    public boolean isEmpty(String... text) {
        for (String string : text) {
            if (string.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void getAllConfig(TextInputEditText cardNumberEdit, TextInputEditText collectUrlEdit, TextInputEditText payUrlEdit, TextInputEditText lockPassEdit, TextInputEditText passStrEdit) {
        String cardNumber = getCardNumber();
        String collectUrl = getCollectUrl();
        String payUrl = getPayUrl();
        String lockPass = getLockPass();
        String passStr = getPASS();

        setText(cardNumber, cardNumberEdit);
        setText(collectUrl, collectUrlEdit);
        setText(payUrl, payUrlEdit);
        setText(lockPass, lockPassEdit);
        setText(passStr, passStrEdit);
    }

    private void setText(String text, TextInputEditText textInputEditText) {
        if (text.isEmpty()) return;
        textInputEditText.setText(text);
    }

    // ========== 批量设置方法 ==========
    public boolean setAllConfig(TextInputEditText cardNumberEdit, TextInputEditText collectUrlEdit, TextInputEditText payUrlEdit, TextInputEditText lockPassEdit, TextInputEditText passStrEdit) {
        Editable cardNumber = cardNumberEdit.getEditableText();
        Editable collectUrl = collectUrlEdit.getEditableText();
        Editable payUrl = payUrlEdit.getEditableText();
        Editable lockPass = lockPassEdit.getEditableText();
        Editable passStr = passStrEdit.getEditableText();
        if (!isEditable(cardNumber, cardNumber, passStr, lockPass, passStr)) {
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CARD_NUMBER, cardNumber.toString());
        editor.putString(LOCK_PASS, lockPass.toString());
        editor.putString(KEY_COLLECT_URL, collectUrl.toString());
        editor.putString(KEY_PAY_URL, payUrl.toString());
        editor.putString(PASS, passStr.toString());
        editor.apply();
        return true;
    }
}