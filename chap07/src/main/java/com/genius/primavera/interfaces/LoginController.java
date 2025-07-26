package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

	private final UserService userService;

	@GetMapping(value = "/login")
	public String loginView() {
		return "login";
	}

	@PostMapping(value = "/login")
	public String logIn(Model model, HttpSession session, HttpServletResponse response, @RequestParam(value = "email") String email, @RequestParam(value = "password") String password) {
		return userService.signIn(email, password) ? signInSuccess(model, session, response, email) : signInFailure(model);
	}

	@GetMapping(value = "/logout")
	public String loginOut(HttpSession httpSession) {
		httpSession.removeAttribute("user");
		return "redirect:/login";
	}

	@NotNull
	private String signInFailure(Model model) {
		model.addAttribute("message", "failure");
		return "login";
	}

	@NotNull
	private String signInSuccess(Model model, HttpSession session, HttpServletResponse response, String email) {
		session.setAttribute("user", userService.findByEmail(email));
		response.addHeader("auth", "success");
		model.addAttribute("message", "success");
		return "redirect:/";
	}
}