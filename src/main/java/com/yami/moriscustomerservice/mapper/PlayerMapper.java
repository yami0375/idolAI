package com.yami.moriscustomerservice.mapper;

import com.yami.moriscustomerservice.pojo.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlayerMapper {
    // 根据条件查询空闲打手
    List<Player> selectAvailablePlayers(
            @Param("level") Integer level,
            @Param("gender") Integer gender,
            @Param("minRating") Double minRating,
            @Param("limit") Integer limit);

    // 根据ID查询打手
    Player selectById(@Param("id") Integer id);

    // 更新打手状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    // 增加完成订单数
    int incrementOrderCount(@Param("id") Integer id);
}

