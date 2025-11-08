package com.xposed.briaccessibilityservice.activity.base.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Description
 * @Author 不一样的风景
 * @Time 2024/11/16 21:04
 */
public class ViewBindingUtil {
    /**
     * @Description 初始化控件实体类
     * @code 1
     * @Author 不一样的风景
     * @Time 2024/11/16 21:03
     */
    public <Binding extends ViewBinding> Binding initBinding(Class<?> CLass, LayoutInflater layoutInflater) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Type type = CLass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            return handleType(types, layoutInflater);
        }
        throw new NoSuchMethodException("null");
    }

    /**
     * @Description 初始化控件实体类
     * @code 1
     * @Author 不一样的风景
     * @Time 2024/11/16 21:03
     */
    public <Binding extends ViewBinding> Binding initBinding(Class<?> CLass, LayoutInflater layoutInflater, ViewGroup viewGroup) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Type type = CLass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            return handleType(types, layoutInflater, viewGroup);
        }
        throw new NoSuchMethodException("null");
    }

    /**
     * @Description 获取泛型类
     * @code 1
     * @Author 不一样的风景
     * @Time 2024/11/16 21:03
     */
    private <Binding extends ViewBinding> Binding handleType(Type[] types, LayoutInflater layoutInflater) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        for (Type type : types) {
            if (type instanceof Class<?>) {
                return handleCLass((Class<?>) type, layoutInflater);
            }
        }
        throw new NoSuchMethodException("null");
    }

    /**
     * @Description 获取泛型类
     * @code 1
     * @Author 不一样的风景
     * @Time 2024/11/16 21:03
     */
    private <Binding extends ViewBinding> Binding handleType(Type[] types, LayoutInflater layoutInflater, ViewGroup viewGroup) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        for (Type type : types) {
            if (type instanceof Class<?>) {
                return handleCLass((Class<?>) type, layoutInflater, viewGroup);
            }
        }
        throw new NoSuchMethodException("null");
    }

    /**
     * @Description 成功获取到泛型，转换控件实体类
     * @code 1
     * @Author 不一样的风景
     * @Time 2024/11/16 21:03
     */
    private <Binding extends ViewBinding> Binding handleCLass(Class<?> aClass, LayoutInflater layoutInflater) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String name = aClass.getName();
        if (name.endsWith("Binding")) {
            return inflate(aClass, layoutInflater);
        }
        throw new NoSuchMethodException("null");
    }

    /**
     * @Description 成功获取到泛型，转换控件实体类
     * @code 1
     * @Author 不一样的风景
     * @Time 2024/11/16 21:03
     */
    private <Binding extends ViewBinding> Binding handleCLass(Class<?> aClass, LayoutInflater layoutInflater, ViewGroup viewGroup) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String name = aClass.getName();
        if (name.endsWith("Binding")) {
            return inflate(aClass, layoutInflater, viewGroup);
        }
        throw new NoSuchMethodException("null");
    }

    public <Binding extends ViewBinding> Binding inflate(Class<?> clazz, LayoutInflater inflater) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return inflate(clazz, inflater, null);
    }

    public <Binding extends ViewBinding> Binding inflate(Class<?> clazz, LayoutInflater inflater, ViewGroup root) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return inflate(clazz, inflater, root, false);
    }

    public <Binding extends ViewBinding> Binding inflate(Class<?> clazz, LayoutInflater inflater, ViewGroup root, boolean attachToRoot) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
        return (Binding) method.invoke(null, inflater, root, attachToRoot);
    }

    public <Binding extends ViewBinding> Binding bind(Class<?> clazz, View view) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getMethod("bind", View.class);
        return (Binding) method.invoke(null, view);
    }
}
