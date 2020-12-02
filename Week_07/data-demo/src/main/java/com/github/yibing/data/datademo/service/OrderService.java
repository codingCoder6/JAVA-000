package com.github.yibing.data.datademo.service;


import com.github.yibing.data.datademo.entity.Order;

import java.util.List;

public interface OrderService {

    int insertOneOrder(Order order);
    int deleteOrder(Order order);
    List<Order> queryOrder();

}
