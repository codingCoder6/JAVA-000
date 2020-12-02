package com.github.yibing.data.datademo2.service;

import com.github.yibing.data.datademo2.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> listProducts();

    int save(Product product);
}
