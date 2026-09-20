package com.disasterconnect.controller;

import com.disasterconnect.entity.User;
import com.disasterconnect.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String registerUser(

            @RequestParam String name,

            @RequestParam String email,

            @RequestParam String password,

            @RequestParam(required = false)
            String role) {

        /*
         * ============================================
         * ROLE SECURITY
         * ============================================
         *
         * Public registration must NEVER be trusted
         * to create an ADMIN account.
         *
         * For now, every public registration becomes
         * a VOLUNTEER account.
         *
         * Coordinator and Administrator accounts will
         * be created through protected administrative
         * functionality later.
         */

        String safeRole = "VOLUNTEER";


        /*
         * ============================================
         * CREATE USER
         * ============================================
         */

        User user = new User(

                name.trim(),

                email.trim().toLowerCase(),

                passwordEncoder.encode(password),

                safeRole

        );


        /*
         * ============================================
         * SAVE USER
         * ============================================
         */

        userService.saveUser(user);


        /*
         * ============================================
         * REDIRECT TO LOGIN
         * ============================================
         */

        return "redirect:/login.html";
    }
}