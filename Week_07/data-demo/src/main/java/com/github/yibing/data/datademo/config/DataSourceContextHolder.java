package com.github.yibing.data.datademo.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceContextHolder {

    private static ThreadLocal<String> dataSourceContext = new ThreadLocal<>();

    public static void switchDataSource(String source) {
        log.info("切换数据源为：" + source);
        dataSourceContext.set(source);
    }

    public static String getDataSource() {
        return dataSourceContext.get();
    }

    public static void clear() {
        dataSourceContext.remove();
    }
}
