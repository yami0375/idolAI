package com.yami.moriscustomerservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Integer id;
    private String orderNo;
    private Integer playerId;
    private Integer customerId;
    private String serviceType;
    private BigDecimal amount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
