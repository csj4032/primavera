package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class LoginController {

	@Autowired
	private UserService userService;

	@GetMapping(value = "/login")
	public String loginView() {
		return "login";
	}

	@PostMapping(value = "/login")
	public String logIn(Model model, HttpSession session, HttpServletResponse response, @RequestParam(value = "email") String email, @RequestParam(value = "password") String password) {
		if (userService.signIn(email, password)) {
			session.setAttribute("user", userService.findByEmail(email));
			response.addHeader("auth", "success");
			model.addAttribute("message", "success");
			return "index";
		} else {
			model.addAttribute("message", "failure");
			return "login";
		}
	}

	@GetMapping(value = "/logout")
	public String loginOut(HttpSession httpSession) {
		httpSession.removeAttribute("user");
		return "redirect:/login";
	}
}