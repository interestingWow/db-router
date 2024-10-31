package com.object.starter.dbrouter.util;

import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class PropertyUtil {

    public static <T> T handle(Environment environment, String prefix, Class<T> targetClass){
        try {
            Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            //获取binderClass类的get方法
            Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
            //获取binderClass类的bind方法
            Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
            //由于是静态方法，所以get方法直接调用传入environment获取到binder对象
            Object binderObject = getMethod.invoke(null, environment);
            String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
            //调用binder对象的bind方法，将属性对应的属性名和值绑定到targetClass这个类里面并且返回一个Binder.Binding
            Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
            //调用用Binding类的get方法，获取到绑定结果
            Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
            return (T) resultGetMethod.invoke(bindResultObject);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                       | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

    }
}
