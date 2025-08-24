package com.example.shirolab.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class InterceptDemoController {

    @GetMapping("/intercept/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }
}
