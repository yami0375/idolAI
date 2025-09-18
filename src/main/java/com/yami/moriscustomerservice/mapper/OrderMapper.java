package com.yami.moriscustomerservice.mapper;


import com.yami.moriscustomerservice.pojo.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {

    // 创建订单
    int insert(Order order);

    // 根据订单编号查询
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    // 更新订单状态
    int updateStatus(
            @Param("orderNo") String orderNo,
            @Param("status") Integer status,
            @Param("endTime") String endTime);
}
