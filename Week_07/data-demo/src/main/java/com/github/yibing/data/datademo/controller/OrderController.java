package com.github.yibing.data.datademo.controller;

import com.github.yibing.data.datademo.entity.Order;
import com.github.yibing.data.datademo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/order/query")
    public List<Order> getOrderList() {
        return orderService.queryOrder();
    }
}
