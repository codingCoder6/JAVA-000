package com.github.yibing.data.datademo.service;

import com.github.yibing.data.datademo.annotation.ReadOnly;
import com.github.yibing.data.datademo.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public int insertOneOrder(Order order) {
        return 0;
    }

    @Override
    public int deleteOrder(Order order) {
        return 0;
    }

    @Override
    @ReadOnly
    public List<Order> queryOrder() {
        String sql = "select * from order_memory limit 10";
        List<Order> maps = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order o = new Order();
            o.setOrderId(rs.getString("order_id"));
            o.setUserId(rs.getString("user_id"));
            o.setAddress(rs.getString("address"));
            o.setAmount(rs.getDouble("amount"));
            o.setStatus(rs.getInt("status"));
            o.setComment(rs.getString("comment"));
            o.setCreateTime(rs.getDate("create_time"));
            o.setUpdateTime(rs.getDate("update_time"));
            return o;
        });
        return maps;
    }
}
