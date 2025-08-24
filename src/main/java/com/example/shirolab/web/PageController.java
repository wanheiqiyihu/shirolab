package com.example.shirolab.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/")
	public String index(Model model) {
		Subject subject = SecurityUtils.getSubject();
		model.addAttribute("principal", subject.getPrincipal());
		model.addAttribute("authenticated", subject.isAuthenticated());
		return "index";
	}

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@GetMapping("/user/profile/page")
	public String userProfilePage(Model model) {
		model.addAttribute("msg", "User Profile Page");
		return "user-profile";
	}

	@GetMapping("/admin/panel/page")
	public String adminPanelPage(Model model) {
		model.addAttribute("msg", "Admin Panel Page");
		return "admin-panel";
	}
}


