package com.example.shirolab.config;

import com.example.shirolab.web.interceptor.RequestLogInterceptor;
import com.example.shirolab.web.interceptor.TestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLogInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**");

        registry.addInterceptor(new TestInterceptor())
                .addPathPatterns("/intercept/**");
    }
}


