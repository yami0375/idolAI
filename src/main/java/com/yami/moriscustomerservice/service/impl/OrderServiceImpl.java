package com.yami.moriscustomerservice.service.impl;

import com.yami.moriscustomerservice.mapper.OrderMapper;
import com.yami.moriscustomerservice.pojo.Order;
import com.yami.moriscustomerservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public String createOrder(Integer playerId, Integer customerId, String serviceType, Double amount) {
        // 生成订单编号
        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setPlayerId(playerId);
        order.setCustomerId(customerId);
        order.setServiceType(serviceType);
        order.setAmount(BigDecimal.valueOf(amount));
        order.setStartTime(LocalDateTime.now());
        order.setStatus(2); // 2表示进行中

        orderMapper.insert(order);
        return orderNo;
    }

    @Override
    public Order getOrderByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public boolean updateOrderStatus(String orderNo, Integer status, String endTime) {
        return orderMapper.updateStatus(orderNo, status, endTime) > 0;
    }

    // 生成唯一订单号
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateTime = LocalDateTime.now().format(formatter);
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + dateTime + random;
    }
}
