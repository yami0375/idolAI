package com.yami.moriscustomerservice.service.impl;

import com.yami.moriscustomerservice.mapper.PlayerMapper;
import com.yami.moriscustomerservice.pojo.Player;
import com.yami.moriscustomerservice.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {
    @Autowired
    private PlayerMapper playerMapper;

    @Override
    public List<Player> findAvailablePlayers(Integer level, Integer gender, Double minRating, Integer limit) {
        return playerMapper.selectAvailablePlayers(level, gender, minRating, limit);
    }

    @Override
    public Player getPlayerById(Integer id) {
        return playerMapper.selectById(id);
    }

    @Override
    public boolean updatePlayerStatus(Integer id, Integer status) {
        return playerMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean incrementPlayerOrderCount(Integer id) {
        return playerMapper.incrementOrderCount(id) > 0;
    }
}
