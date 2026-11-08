package com.parking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    // 系统首页（登录后跳转目标）
    @GetMapping("/index")
    public String index() {
        return "index";
    }
}
