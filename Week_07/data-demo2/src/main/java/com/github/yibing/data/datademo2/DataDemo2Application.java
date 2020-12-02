package com.github.yibing.data.datademo2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.github.yibing.data.datademo2.dao")
public class DataDemo2Application {

    public static void main(String[] args) {
        SpringApplication.run(DataDemo2Application.class, args);
    }

}
