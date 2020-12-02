package com.github.yibing.data.datademo2.service;

import com.github.yibing.data.datademo2.dao.ProductMapper;
import com.github.yibing.data.datademo2.entity.Product;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public List<Product> listProducts() {
        return productMapper.listProducts();
    }

    @Override
    public int save(Product product) {
        return productMapper.insert(product);
    }
}
