package com.yami.moriscustomerservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private Integer id;
    private String username;
    private Integer level;
    private Integer gender;
    private BigDecimal rating;
    private BigDecimal kd;
    private Integer orderCount;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
