package com.github.yibing.data.datademo2.dao;

import com.github.yibing.data.datademo2.entity.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> listProducts();

    int insert(Product product);
}
