package com.yami.moriscustomerservice.service;

import com.yami.moriscustomerservice.pojo.Player;

import java.util.List;

public interface PlayerService {
    // 查询符合条件的空闲打手
    List<Player> findAvailablePlayers(Integer level, Integer gender, Double minRating, Integer limit);

    // 根据ID查询打手详情
    Player getPlayerById(Integer id);

    // 更新打手状态
    boolean updatePlayerStatus(Integer id, Integer status);

    // 增加打手完成订单数
    boolean incrementPlayerOrderCount(Integer id);
}
