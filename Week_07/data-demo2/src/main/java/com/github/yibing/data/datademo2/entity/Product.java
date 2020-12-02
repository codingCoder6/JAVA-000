package com.github.yibing.data.datademo2.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Product {
    private String productId;
    private double price;
    private String productName;
    private Date createTime;
    private Date updateTime;
    private String description;
    private String productType;
    private Integer status;
}
