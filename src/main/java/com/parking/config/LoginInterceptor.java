package com.parking.config;

import com.parking.entity.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：未登录用户访问业务页面时强制跳转登录页
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        // 已登录则放行
        if (loginUser != null) {
            return true;
        }
        // 未登录：重定向到登录页
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return false;
    }
}
