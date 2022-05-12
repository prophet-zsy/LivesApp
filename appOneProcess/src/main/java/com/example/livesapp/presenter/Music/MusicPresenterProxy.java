package com.example.livesapp.presenter.Music;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * MusicPresenter代理类
 * 负责权限管理、日志管理
 * (权限管理实现原理：一个代理对应一个handler，对应的权限维护在handler中)
 */

public class MusicPresenterProxy implements InvocationHandler {
    private static MusicPresenter musicPresenter = MusicPresenter.getInstance();

    private Set<Class> permissions;

    private MusicPresenterProxy(Class[] interfaces) {
        permissions = new HashSet<>(Arrays.asList(interfaces));
    }

    public static IMusicPresenter getProxy(Class[] interfaces) {
        IMusicPresenter proxy = (IMusicPresenter) Proxy.newProxyInstance(MusicPresenterProxy.class.getClassLoader(),
                musicPresenter.getClass().getInterfaces(),
                new MusicPresenterProxy(interfaces));
        return proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.i("musicPresenter", "invoke: " + method.getName() + Arrays.toString(args));
        Class<?> declaringClass = method.getDeclaringClass();
        if (permissions.contains(declaringClass)) return method.invoke(musicPresenter, args);
        else throw new RuntimeException("you have no permission of interface : " + declaringClass.getName());
    }
}
