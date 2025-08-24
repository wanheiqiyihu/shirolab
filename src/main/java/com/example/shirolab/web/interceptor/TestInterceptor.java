package com.example.shirolab.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class TestInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(TestInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long start = System.currentTimeMillis();
        request.setAttribute("__ti_start", start);
        // 提前写入一个可见的响应头，确保客户端能看到
        response.addHeader("X-Test-Interceptor", "hit");
        log.info("[TestInterceptor] preHandle: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Object st = request.getAttribute("__ti_start");
        if (st instanceof Long) {
            long cost = System.currentTimeMillis() - (Long) st;
            // 覆盖为最终耗时（若响应未提交）
            response.setHeader("X-Test-Interceptor", "cost=" + cost + "ms");
            log.info("[TestInterceptor] postHandle: {} {}, cost={}ms", request.getMethod(), request.getRequestURI(), cost);
        }
    }
}
