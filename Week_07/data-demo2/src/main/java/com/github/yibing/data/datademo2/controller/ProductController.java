package com.github.yibing.data.datademo2.controller;

import com.github.yibing.data.datademo2.entity.Product;
import com.github.yibing.data.datademo2.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/product/queryAll")
    public List<Product> listProducts(){
        return productService.listProducts();
    }

    @PostMapping("/product/save")
    public int saveProduct(@RequestBody Product product){
        return productService.save(product);
    }
}
