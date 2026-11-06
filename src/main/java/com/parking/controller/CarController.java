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
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/car")
public class CarController {

    @Autowired
    private ParkingCarRecordMapper recordMapper;
    @Autowired
    private ParkingSpaceMapper spaceMapper;
    @Autowired
    private ParkingOrderMapper orderMapper;
    @Autowired
    private ParkingBlacklistMapper blacklistMapper;

    // 车辆入场登记
    @PostMapping("/enter")
    public String carEnter(String carNum, Integer carType, HttpSession session) {
        // 黑名单校验
        if (blacklistMapper.selectOne(new LambdaQueryWrapper<ParkingBlacklist>()
                .eq(ParkingBlacklist::getCarNum, carNum)) != null) {
            return "redirect:/car/record?msg=黑名单车辆，禁止入场";
        }
        // 找空闲车位
        ParkingSpace space = spaceMapper.selectOne(new LambdaQueryWrapper<ParkingSpace>()
                .eq(ParkingSpace::getStatus, 0)
                .last("limit 1"));
        if (space == null) return "redirect:/car/record?msg=暂无空闲车位";

        // 入场记录
        ParkingCarRecord record = new ParkingCarRecord();
        record.setCarNum(carNum);
        record.setCarType(carType);
        record.setEnterTime(LocalDateTime.now());
        record.setSpaceId(space.getSpaceId());
        SysUser user = (SysUser) session.getAttribute("loginUser");
        record.setOperator(user.getRealName());
        recordMapper.insert(record);

        // 车位状态更新
        space.setStatus(1);
        space.setCarNum(carNum);
        spaceMapper.updateById(space);

        // 生成订单
        ParkingOrder order = new ParkingOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-","").substring(0,20));
        order.setCarNum(carNum);
        order.setEnterTime(LocalDateTime.now());
        order.setSpaceId(space.getSpaceId());
        order.setPayStatus(0);
        orderMapper.insert(order);

        return "redirect:/car/record";
    }

    // 进出记录列表
    @GetMapping("/record")
    public String recordList(Model model, String carNum) {
        LambdaQueryWrapper<ParkingCarRecord> wrapper = new LambdaQueryWrapper<>();
        if (carNum != null && !carNum.isEmpty()) wrapper.like(ParkingCarRecord::getCarNum, carNum);
        wrapper.orderByDesc(ParkingCarRecord::getEnterTime);
        model.addAttribute("list", recordMapper.selectList(wrapper));
        return "car/record";
    }
}