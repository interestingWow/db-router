package com.object.starter.dbrouter.dynamic;

import com.object.starter.dbrouter.DBContextHolder;
import com.object.starter.dbrouter.annotation.DBRouter;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.javassist.tools.reflect.Metaobject;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DynamicDataSourcePlugin implements Interceptor {
    /**
     * 密钥，必须是16位
     */
    private static final String KEY = "1898794876567654";
    /**
     * 偏移量，必须是16位
     */
    private static final String IV = "1233214566547891";

    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
        MetaObject paramMetaObject = MetaObject.forObject(parameterHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        Object parameterObject = paramMetaObject.getValue("parameterObject");
        //判断这个方法是否需要分库分表
        System.out.println(invocation.getMethod().getName());
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        String methodName = id.substring(id.lastIndexOf(".") + 1);
        Class<?> clazz = Class.forName(className);
        if(clazz.getMethod(methodName, parameterObject.getClass()).isAnnotationPresent(DBRouter.class)){
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            //替换掉sql中的tbIdx部分
            String replaceSql = sql.replace("tbIdx", DBContextHolder.getTBKey());
            //通过反射去修改sql语句
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, replaceSql);
            field.setAccessible(false);
            System.out.println("替换完成 " + replaceSql);
        }


        return invocation.proceed();
    }
}