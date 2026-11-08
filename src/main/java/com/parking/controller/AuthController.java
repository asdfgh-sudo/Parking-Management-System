package com.parking.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.entity.SysUser;
import com.parking.mapper.SysUserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserMapper userMapper;

    // 登录页
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 登录提交
    @PostMapping("/doLogin")
    public String doLogin(String username, String password, HttpSession session, Model model) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getStatus, 1));
        if (user == null || !password.equals(user.getPassword())) {
            model.addAttribute("msg", "账号或密码错误");
            return "login";
        }
        session.setAttribute("loginUser", user);
        return "redirect:/index";
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // 用户管理（管理员权限）
    @GetMapping("/userList")
    public String userList(Model model, HttpSession session) {
    	SysUser user = (SysUser) session.getAttribute("loginUser");
    	// 先判断是否登录，再判断角色
    	if (user == null || user.getRoleId() != 1) {
    	    return "redirect:/index";
    	}
        model.addAttribute("list", userMapper.selectList(null));
        return "user/list";
    }

    // 新增用户（管理员权限）
    @PostMapping("/userAdd")
    public String userAdd(String username, String password, String realName, Integer roleId, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() != 1) {
            return "redirect:/index";
        }
        // 账号重名校验
        SysUser exist = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (exist != null) {
            return "redirect:/auth/userList?msg=账号已存在";
        }
        SysUser newUser = new SysUser();
        newUser.setUsername(username);
        newUser.setPassword(password == null || password.isEmpty() ? "123456" : password);
        newUser.setRealName(realName);
        newUser.setRoleId(roleId == null ? 2 : roleId);
        newUser.setStatus(1);
        userMapper.insert(newUser);
        return "redirect:/auth/userList";
    }

    // 启用 / 禁用账号（管理员权限）
    @GetMapping("/userStatus/{id}/{status}")
    public String userStatus(@PathVariable Integer id, @PathVariable Integer status, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() != 1) {
            return "redirect:/index";
        }
        // 禁止禁用自己
        if (id.equals(loginUser.getUserId())) {
            return "redirect:/auth/userList?msg=不能操作当前登录账号";
        }
        SysUser target = new SysUser();
        target.setUserId(id);
        target.setStatus(status == 1 ? 1 : 0);
        userMapper.updateById(target);
        return "redirect:/auth/userList";
    }

    // 删除账号（管理员权限）
    @GetMapping("/userDelete/{id}")
    public String userDelete(@PathVariable Integer id, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() != 1) {
            return "redirect:/index";
        }
        if (id.equals(loginUser.getUserId())) {
            return "redirect:/auth/userList?msg=不能删除当前登录账号";
        }
        userMapper.deleteById(id);
        return "redirect:/auth/userList";
    }
}