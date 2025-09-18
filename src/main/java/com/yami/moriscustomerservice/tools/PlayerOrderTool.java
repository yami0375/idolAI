package com.yami.moriscustomerservice.tools;


import com.yami.moriscustomerservice.pojo.Order;
import com.yami.moriscustomerservice.pojo.Player;
import com.yami.moriscustomerservice.service.OrderService;
import com.yami.moriscustomerservice.service.PlayerService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlayerOrderTool {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private OrderService orderService;

    /**
     * 查询符合条件的空闲打手
     * @param level 打手等级（1-娱乐/2-金牌/3-百榜/4-巅峰）
     * @param gender 性别（1-男/2-女，可选）
     * @param minRating 最低评分（可选）
     * @param limit 最大数量（可选）
     * @return 空闲打手列表
     */
    @Tool("查询空闲打手，可按等级、性别、最低评分筛选，返回符合条件的打手信息")
    public String queryAvailablePlayers(
            @P("打手等级（1-娱乐/2-金牌/3-百榜/4-巅峰，必填）") Integer level,
            @P("性别（1-男/2-女，可选）") Integer gender,
            @P("最低评分（0-5之间，可选）") Double minRating,
            @P("最多返回数量，可选") Integer limit) {

        List<Player> players = playerService.findAvailablePlayers(level, gender, minRating, limit);

        if (players.isEmpty()) {
            return "没有找到符合条件的空闲打手";
        }

        return players.stream()
                .map(p -> String.format(
                        "ID: %d, 昵称: %s, 等级: %d, 性别: %s, 评分: %.2f, KD: %.2f, 完成订单: %d",
                        p.getId(), p.getUsername(), p.getLevel(),
                        p.getGender() == 1 ? "男" : "女",
                        p.getRating(), p.getKd(), p.getOrderCount()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 创建新订单
     * @param playerId 打手ID
     * @param customerId 客户ID
     * @param serviceType 服务类型
     * @param amount 订单金额
     * @return 订单编号
     */
    @Tool("创建新订单，需要指定打手ID、客户ID、服务类型和金额")
    public String createOrder(
            @P("打手ID，必填") Integer playerId,
            @P("客户ID，必填") Integer customerId,
            @P("服务类型，如：护航、陪玩等，必填") String serviceType,
            @P("订单金额，单位：元，必填") Double amount) {

        // 检查打手是否存在且空闲
        Player player = playerService.getPlayerById(playerId);
        if (player == null) {
            return "打手不存在";
        }
        if (player.getStatus() != 1) {
            return "该打手当前不空闲，无法接单";
        }

        // 创建订单
        String orderNo = orderService.createOrder(playerId, customerId, serviceType, amount);

        // 更新打手状态为忙碌
        playerService.updatePlayerStatus(playerId, 2);

        return "订单创建成功，订单编号：" + orderNo;
    }

    /**
     * 完成订单
     * @param orderNo 订单编号
     * @return 操作结果
     */
    @Tool("完成指定订单，更新订单状态并将打手设为空闲")
    public String completeOrder(
            @P("订单编号，必填") String orderNo) {

        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) {
            return "订单不存在";
        }

        if (order.getStatus() == 3) {
            return "该订单已完成";
        }

        if (order.getStatus() == 4) {
            return "该订单已取消";
        }

        // 更新订单状态为已完成
        String endTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        orderService.updateOrderStatus(orderNo, 3, endTime);

        // 更新打手状态为空闲
        playerService.updatePlayerStatus(order.getPlayerId(), 1);

        // 增加打手完成订单数
        playerService.incrementPlayerOrderCount(order.getPlayerId());

        return "订单已完成，订单编号：" + orderNo;
    }

    /**
     * 取消订单
     * @param orderNo 订单编号
     * @return 操作结果
     */
    @Tool("取消指定订单，更新订单状态并将打手设为空闲")
    public String cancelOrder(
            @P("订单编号，必填") String orderNo) {

        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) {
            return "订单不存在";
        }

        if (order.getStatus() == 3) {
            return "该订单已完成，无法取消";
        }

        if (order.getStatus() == 4) {
            return "该订单已取消";
        }

        // 更新订单状态为已取消
        orderService.updateOrderStatus(orderNo, 4, null);

        // 更新打手状态为空闲
        playerService.updatePlayerStatus(order.getPlayerId(), 1);

        return "订单已取消，订单编号：" + orderNo;
    }

    /**
     * 查询订单详情
     * @param orderNo 订单编号
     * @return 订单详情
     */
    @Tool("查询指定订单的详细信息")
    public String queryOrder(
            @P("订单编号，必填") String orderNo) {

        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) {
            return "订单不存在";
        }

        // 获取打手信息
        Player player = playerService.getPlayerById(order.getPlayerId());
        String playerName = player != null ? player.getUsername() : "未知";

        String statusDesc = getStatusDesc(order.getStatus());

        return String.format(
                "订单编号：%s\n打手：%s(ID:%d)\n客户ID：%d\n服务类型：%s\n金额：%.2f元\n开始时间：%s\n结束时间：%s\n状态：%s",
                order.getOrderNo(), playerName, order.getPlayerId(),
                order.getCustomerId(), order.getServiceType(), order.getAmount(),
                order.getStartTime(), order.getEndTime(), statusDesc);
    }

    // 转换订单状态为描述文字
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 1: return "待开始";
            case 2: return "进行中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }
}
