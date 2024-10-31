package com.object.starter.dbrouter.config;

import com.object.starter.dbrouter.DBRouterConfig;
import com.object.starter.dbrouter.dynamic.DynamicDataSource;
import com.object.starter.dbrouter.dynamic.DynamicDataSourcePlugin;
import com.object.starter.dbrouter.util.PropertyUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class DBAutoConfiguration implements EnvironmentAware {
    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    private int dbCount;  //分库数
    private int tbCount;  //分表数

    @Override
    public void setEnvironment(Environment environment) {
        //在这里获取动态数据源的配置并且注入到map里面
        String prefix = "dbrouter.jdbc.datasource.";
        dbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "dbCount")));
        tbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "tbCount")));


        //获取有几个数据源 配置上是用 , 隔开
        String dataSource = Objects.requireNonNull(environment.getProperty(prefix + "list"));

        String[] dataSources = dataSource.split(",");
        for (String dbInfo : dataSources) {
            //将配置文件中的属性绑定到一个map里面，方便取用
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }
    }

    @Bean
    public DataSource dataSource(){
        HashMap<Object, Object> targetDatasource = new HashMap<>();

        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> map = dataSourceMap.get(dbInfo);
            targetDatasource.put(dbInfo, new DriverManagerDataSource(map.get("url").toString(), map.get("username").toString(), map.get("password").toString()));
        }

        //设置动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDatasource);

        return dynamicDataSource;
    }

    @Bean
    public DBRouterConfig dbRouterConfig(){
        return new DBRouterConfig(dbCount, tbCount);
    }

    @Bean
    public String addInterceptor(SqlSessionFactory sqlSessionFactory){
        DynamicDataSourcePlugin plugin = new DynamicDataSourcePlugin();
        sqlSessionFactory.getConfiguration().addInterceptor(plugin);
        return "";
    }
}
