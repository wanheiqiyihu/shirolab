package com.example.shirolab.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class AuthController {

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginJson(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Map<String, Object> body = new HashMap<String, Object>();
        try {
            subject.login(token);
            body.put("message", "login success");
            body.put("user", username);
            return ResponseEntity.ok(body);
        } catch (UnknownAccountException e) {
            body.put("message", "unknown account");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        } catch (IncorrectCredentialsException e) {
            body.put("message", "incorrect credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        } catch (LockedAccountException e) {
            body.put("message", "account locked");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }

    @PostMapping("/login/form")
    public String loginForm(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            @RequestParam(value = "rememberMe", required = false) Boolean rememberMe) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        if (rememberMe != null) {
            token.setRememberMe(rememberMe);
        }
        try {
            subject.login(token);
            return "redirect:/";
        } catch (UnknownAccountException | IncorrectCredentialsException e) {
            return "redirect:/login?error";
        } catch (LockedAccountException e) {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/me")
    @ResponseBody
    public Map<String, Object> me() {
        Subject subject = SecurityUtils.getSubject();
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("authenticated", subject.isAuthenticated());
        body.put("principal", subject.getPrincipal());
        body.put("roles", new String[]{
                subject.hasRole("admin") ? "admin" : null,
                subject.hasRole("user") ? "user" : null
        });
        return body;
    }

    @GetMapping("/user/profile")
    @RequiresRoles("user")
    public Map<String, Object> userProfile() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("profile", "This is user profile");
        return body;
    }

    @GetMapping("/admin/panel")
    @RequiresRoles("admin")
    public Map<String, Object> adminPanel() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("panel", "This is admin panel");
        return body;
    }
}


