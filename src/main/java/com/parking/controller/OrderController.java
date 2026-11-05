package com.parking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.parking.entity.*;
import com.parking.mapper.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private ParkingOrderMapper orderMapper;
    @Autowired
    private ChargingRuleMapper ruleMapper;
    @Autowired
    private ParkingSpaceMapper spaceMapper;
    @Autowired
    private ParkingCarRecordMapper recordMapper;

    // 订单列表
    @GetMapping("/list")
    public String orderList(Model model, Integer payStatus, String carNum) {
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<>();
        if (payStatus != null) wrapper.eq(ParkingOrder::getPayStatus, payStatus);
        if (carNum != null && !carNum.isEmpty()) wrapper.like(ParkingOrder::getCarNum, carNum);
        wrapper.orderByDesc(ParkingOrder::getEnterTime);
        model.addAttribute("list", orderMapper.selectList(wrapper));
        return "order/list";
    }

    // 计算费用 + 结算缴费
    @GetMapping("/pay/{orderId}")
    public String payOrder(@PathVariable Integer orderId) {
        ParkingOrder order = orderMapper.selectById(orderId);
        if (order.getPayStatus() == 1) return "redirect:/order/list";

        // 获取收费规则
        ChargingRule rule = ruleMapper.selectById(1);
        // 计算停车时长
        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(order.getEnterTime(), now).toMinutes();
        long hours = (long) Math.ceil(minutes / 60.0);

        // 阶梯计费
        BigDecimal amount;
        if (hours <= 1) {
            amount = rule.getFirstHourPrice();
        } else {
            amount = rule.getFirstHourPrice()
                    .add(rule.getPerHourPrice().multiply(BigDecimal.valueOf(hours - 1)));
        }
        // 单日封顶
        if (amount.compareTo(rule.getDayMaxPrice()) > 0) amount = rule.getDayMaxPrice();

        // 更新订单
        order.setExitTime(now);
        order.setParkDuration((int) minutes);
        order.setTotalAmount(amount);
        order.setPayStatus(1);
        order.setPayTime(now);
        orderMapper.updateById(order);

        // 释放车位
        ParkingSpace space = new ParkingSpace();
        space.setSpaceId(order.getSpaceId());
        space.setStatus(0);
        space.setCarNum(null);
        spaceMapper.updateById(space);

        // 更新出场记录
        recordMapper.update(null, new LambdaUpdateWrapper<ParkingCarRecord>()
                .eq(ParkingCarRecord::getCarNum, order.getCarNum())
                .isNull(ParkingCarRecord::getExitTime)
                .set(ParkingCarRecord::getExitTime, now));

        return "redirect:/order/list";
    }

    // 收费规则页面（仅超级管理员可访问）
    @GetMapping("/rule")
    public String rulePage(Model model, HttpSession session) {
        // 非管理员访问收费规则页则跳转系统首页
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() != 1) {
            return "redirect:/index";
        }
        ChargingRule rule = ruleMapper.selectById(1);
        if (rule == null) {
            rule = new ChargingRule();
            rule.setFirstHourPrice(new BigDecimal("5.00"));
            rule.setPerHourPrice(new BigDecimal("2.00"));
            rule.setDayMaxPrice(new BigDecimal("20.00"));
            rule.setMonthPrice(new BigDecimal("300.00"));
            ruleMapper.insert(rule);
        }
        model.addAttribute("rule", rule);
        return "order/rule";
    }

    // 收费规则配置（仅超级管理员可操作）
    @PostMapping("/rule/update")
    public String updateRule(ChargingRule rule, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() != 1) {
            return "redirect:/index";
        }
        ruleMapper.updateById(rule);
        return "redirect:/order/rule";
    }
}