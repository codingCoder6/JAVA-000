package com.github.yibing.data.datademo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Order {
    private String orderId;
    private String userId;
    private String address;
    private double amount;
    private int status;
    private Date createTime;
    private Date updateTime;
    private String comment;
}
