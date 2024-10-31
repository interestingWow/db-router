package com.object.starter.dbrouter.dynamic;

import com.object.starter.dbrouter.DBContextHolder;
import com.object.starter.dbrouter.annotation.DBRouter;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
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

        //判断这个方法是否需要分库分表
        if(invocation.getMethod().isAnnotationPresent(DBRouter.class)){
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            //替换掉sql中的tbIdx部分
            String replaceSql = sql.replace("#{tbIdx}", DBContextHolder.getTBKey());
            //通过反射去修改sql语句
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, replaceSql);
            field.setAccessible(false);
        }

        return invocation.proceed();
    }
}