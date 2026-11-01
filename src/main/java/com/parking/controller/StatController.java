package com.parking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.entity.ParkingBlacklist;
import com.parking.entity.ParkingOrder;
import com.parking.mapper.ParkingBlacklistMapper;
import com.parking.mapper.ParkingOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/stat")
public class StatController {

    @Autowired
    private ParkingOrderMapper orderMapper;
    @Autowired
    private ParkingBlacklistMapper blacklistMapper;

    // 数据统计首页
    @GetMapping("/index")
    public String statIndex(Model model) {
        // 今日订单数
        long todayOrder = orderMapper.selectCount(new LambdaQueryWrapper<ParkingOrder>()
                .ge(ParkingOrder::getEnterTime, LocalDate.now().atStartOfDay()));
        // 今日营收
        List<ParkingOrder> todayPaid = orderMapper.selectList(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getPayStatus, 1)
                .ge(ParkingOrder::getPayTime, LocalDate.now().atStartOfDay()));
        BigDecimal todayIncome = todayPaid.stream()
                .map(ParkingOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 未支付订单数
        long unpaidCount = orderMapper.selectCount(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getPayStatus, 0));
        // 累计订单数
        long totalOrder = orderMapper.selectCount(null);
        // 累计营收（全部已支付订单金额之和）
        List<ParkingOrder> allPaid = orderMapper.selectList(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getPayStatus, 1));
        BigDecimal totalIncome = allPaid.stream()
                .map(ParkingOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 今日订单明细
        List<ParkingOrder> todayList = orderMapper.selectList(new LambdaQueryWrapper<ParkingOrder>()
                .ge(ParkingOrder::getEnterTime, LocalDate.now().atStartOfDay())
                .orderByDesc(ParkingOrder::getEnterTime)
                .last("limit 20"));

        model.addAttribute("todayOrder", todayOrder);
        model.addAttribute("todayIncome", todayIncome);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("totalOrder", totalOrder);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("blackCount", blacklistMapper.selectCount(null));
        model.addAttribute("todayList", todayList);
        return "stat/index";
    }

    // 黑名单列表
    @GetMapping("/blacklist")
    public String blacklist(Model model) {
        model.addAttribute("list", blacklistMapper.selectList(null));
        return "stat/blacklist";
    }

    // 添加黑名单
    @PostMapping("/blacklist/add")
    public String addBlack(String carNum, String reason) {
        ParkingBlacklist black = new ParkingBlacklist();
        black.setCarNum(carNum);
        black.setReason(reason);
        blacklistMapper.insert(black);
        return "redirect:/stat/blacklist";
    }

    // 移除黑名单
    @GetMapping("/blacklist/remove/{id}")
    public String removeBlack(@PathVariable Integer id) {
        blacklistMapper.deleteById(id);
        return "redirect:/stat/blacklist";
    }
}