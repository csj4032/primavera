package com.genius.primavera.interfaces;

import com.genius.primavera.application.bulk.BulkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final BulkService bulkService;

    @GetMapping(value = "/login")
    public String loginForm() {
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

    @GetMapping(value = "/bulk")
    public String bulk() throws Exception {
        bulkService.bulkInsert();
        return "index";
    }
}