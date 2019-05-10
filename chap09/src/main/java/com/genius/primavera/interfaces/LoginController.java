package com.genius.primavera.interfaces;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Slf4j
@Controller
public class LoginController {

    @GetMapping(value = "/login")
    public String loginForm(HttpServletRequest httpServletRequest) {
        System.out.println(httpServletRequest.getScheme());
        System.out.println(httpServletRequest.getHeader("X-Forwarded-Proto"));
        return "login";
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/index")
    public String index() {
        return "index";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/manager")
    public String manager() {
        return "manager";
    }

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/admin")
    public String admin() {
        return "admin";
    }
}