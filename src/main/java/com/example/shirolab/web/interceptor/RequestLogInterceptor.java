package com.example.shirolab.web.interceptor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        request.setAttribute("_startTime", System.currentTimeMillis());
        log.info("Incoming {} {} user={}", request.getMethod(), request.getRequestURI(), principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Object startAttr = request.getAttribute("_startTime");
        if (startAttr instanceof Long) {
            long cost = System.currentTimeMillis() - (Long) startAttr;
            log.info("Completed {} {} status={} costMs={}", request.getMethod(), request.getRequestURI(), response.getStatus(), cost);
        }
    }
}


