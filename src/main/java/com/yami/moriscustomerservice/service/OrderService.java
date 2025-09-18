package com.yami.moriscustomerservice.service;

import com.yami.moriscustomerservice.pojo.Order;

public interface OrderService {
    // 创建新订单
    String createOrder(Integer playerId, Integer customerId, String serviceType, Double amount);

    // 根据订单编号查询订单
    Order getOrderByNo(String orderNo);

    // 更新订单状态
    boolean updateOrderStatus(String orderNo, Integer status, String endTime);
}
