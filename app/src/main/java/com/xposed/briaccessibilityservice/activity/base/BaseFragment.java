package com.xposed.briaccessibilityservice.activity.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.xposed.jagohook.activity.base.utils.ViewBindingUtil;

import java.lang.reflect.InvocationTargetException;

public class BaseFragment<T extends ViewBinding> extends Fragment {
    public T binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = getBinding(inflater, container);
        return binding.getRoot();
    }

    public T getBinding(LayoutInflater inflater, ViewGroup container) {
        ViewBindingUtil util = new ViewBindingUtil();
        try {
            return util.initBinding(getClass(), inflater, container);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
