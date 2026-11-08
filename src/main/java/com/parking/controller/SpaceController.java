package com.parking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.entity.ParkingSpace;
import com.parking.mapper.ParkingSpaceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/space")
public class SpaceController {

    @Autowired
    private ParkingSpaceMapper spaceMapper;

    // 车位列表
    @GetMapping("/list")
    public String list(Model model, Integer status, String areaType) {
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(ParkingSpace::getStatus, status);
        if (areaType != null && !areaType.isEmpty()) wrapper.eq(ParkingSpace::getAreaType, areaType);
        model.addAttribute("list", spaceMapper.selectList(wrapper));
        model.addAttribute("freeCount", spaceMapper.selectCount(new LambdaQueryWrapper<ParkingSpace>().eq(ParkingSpace::getStatus, 0)));
        model.addAttribute("occupiedCount", spaceMapper.selectCount(new LambdaQueryWrapper<ParkingSpace>().eq(ParkingSpace::getStatus, 1)));
        model.addAttribute("faultCount", spaceMapper.selectCount(new LambdaQueryWrapper<ParkingSpace>().eq(ParkingSpace::getStatus, 2)));
        model.addAttribute("totalCount", spaceMapper.selectCount(null));
        return "space/list";
    }

    // 新增车位
    @PostMapping("/add")
    public String add(ParkingSpace space) {
        space.setStatus(0);
        spaceMapper.insert(space);
        return "redirect:/space/list";
    }

    // 标记故障/恢复
    @GetMapping("/updateStatus/{id}/{status}")
    public String updateStatus(@PathVariable Integer id, @PathVariable Integer status) {
        ParkingSpace space = new ParkingSpace();
        space.setSpaceId(id);
        space.setStatus(status);
        spaceMapper.updateById(space);
        return "redirect:/space/list";
    }

    // 删除车位
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        spaceMapper.deleteById(id);
        return "redirect:/space/list";
    }
}