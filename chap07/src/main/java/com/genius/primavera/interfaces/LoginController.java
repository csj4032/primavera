package com.genius.primavera.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class LoginController {

    @GetMapping(value = "/login")
    public String loginView() {
        return "login";
    }

    @PostMapping(value = "/login")
    public String logIn(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("principal", authentication.getPrincipal());
        model.addAttribute("credentials", authentication.getCredentials());
        return "index";
    }

    @GetMapping(value = "/logout")
    public String loginOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("Invalidating session: {}", session.getId());
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}