package com.github.yibing.data.datademo.annotation;

import com.github.yibing.data.datademo.config.DataSourceContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(com.github.yibing.data.datademo.annotation.ReadOnly)")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        ReadOnly annotation = method.getAnnotation(ReadOnly.class);
        if (annotation != null) {
            DataSourceContextHolder.switchDataSource(annotation.name());
        }
        Annotation[] annotations = joinPoint.getTarget().getClass().getAnnotations();
        System.out.println(annotations);
    }

    @After("pointcut()")
    public void after() {
        DataSourceContextHolder.clear();
    }
}
