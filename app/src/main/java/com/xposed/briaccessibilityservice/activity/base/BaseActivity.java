package com.xposed.briaccessibilityservice.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.xposed.jagohook.activity.base.utils.ViewBindingUtil;

import java.lang.reflect.InvocationTargetException;

import lombok.Getter;


/**
 * @Description 统一继承类
 * @Author 不一样的风景
 * @Time 2024/12/8 18:12
 */
public class BaseActivity<T extends ViewBinding> extends AppCompatActivity implements ActivityResultCallback<ActivityResult>, Handler.Callback {
    public T binding;
    @Getter
    private ActivityResultLauncher<Intent> launcher;
    @Getter
    private final Handler handler = new Handler(Looper.getMainLooper(), this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
        binding = getBinding();
        setContentView(binding.getRoot());
    }

    public T getBinding() {
        ViewBindingUtil util = new ViewBindingUtil();
        try {
            return util.initBinding(getClass(), getLayoutInflater());
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onActivityResult(ActivityResult o) {

    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        return false;
    }
}
