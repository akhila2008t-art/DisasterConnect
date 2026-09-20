package com.disasterconnect.config;

import com.disasterconnect.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;


    public SecurityConfig(
            CustomUserDetailsService userDetailsService) {

        this.userDetailsService =
                userDetailsService;
    }


    /* =====================================================
       PASSWORD ENCODER
    ===================================================== */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    /* =====================================================
       AUTHENTICATION PROVIDER
    ===================================================== */

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }


    /* =====================================================
       SECURITY FILTER CHAIN
    ===================================================== */

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http


            /* =================================================
               CSRF
            ================================================= */

            .csrf(csrf -> csrf

                .ignoringRequestMatchers(

                    "/register",

                    "/login",

                    "/logout",

                    "/emergency-request",

                    "/volunteer/register",

                    "/resource/add",

                    "/api/**"

                )
            )


            /* =================================================
               AUTHORIZATION
            ================================================= */

            .authorizeHttpRequests(auth -> auth


                /* ================= ADMIN ================= */

                .requestMatchers(
                    "/api/admin/**"
                )
                .hasRole("ADMIN")


                /* ================= PUBLIC PAGES ================= */

                .requestMatchers(

                    "/",

                    "/index.html",

                    "/login.html",

                    "/register.html",

                    "/emergency-request.html",

                    "/volunteer-register.html"

                )
                .permitAll()


                /* ================= PUBLIC ACTIONS ================= */

                .requestMatchers(

                    "/register",

                    "/login",

                    "/emergency-request",

                    "/volunteer/register"

                )
                .permitAll()


                /* ================= STATIC FILES ================= */

                .requestMatchers(

                    "/css/**",

                    "/js/**",

                    "/images/**",

                    "/favicon.ico"

                )
                .permitAll()


                /* ================= PUBLIC API ================= */

                .requestMatchers(

                    "/api/geocode/**",

                    "/api/statistics"

                )
                .permitAll()


                /* ================= EMERGENCY LIST ================= */

                .requestMatchers(

                    "/api/emergency-requests"

                )
                .permitAll()


                /* ================= RESOURCE ADD ================= */

                .requestMatchers(

                    "/resource/add"

                )
                .hasAnyRole(

                    "ADMIN",

                    "COORDINATOR"

                )


                /* ================= RESOURCE MANAGEMENT ================= */

                .requestMatchers(

                    "/api/resources/*/status",

                    "/api/resources/*/quantity",

                    "/api/resources/*"

                )
                .hasAnyRole(

                    "ADMIN",

                    "COORDINATOR"

                )


                /* ================= RESOURCE PAGE ================= */

                .requestMatchers(

                    "/resources.html",

                    "/api/resources",

                    "/api/resources/**"

                )
                .authenticated()


                /* ================= EMERGENCY MANAGEMENT ================= */

                .requestMatchers(

                    "/api/emergency-requests/*/status",

                    "/api/emergency-requests/*/assign",

                    "/api/emergency-requests/*"

                )
                .hasAnyRole(

                    "ADMIN",

                    "COORDINATOR"

                )


                /* ================= VOLUNTEER MANAGEMENT ================= */

                .requestMatchers(

                    "/api/volunteers/*/status",

                    "/api/volunteers/*/availability"

                )
                .hasAnyRole(

                    "ADMIN",

                    "COORDINATOR"

                )


                .requestMatchers(

                    "/api/volunteers/*"

                )
                .hasAnyRole(

                    "ADMIN",

                    "COORDINATOR"

                )


                /* ================= VOLUNTEER API ================= */

                .requestMatchers(

                    "/api/volunteers",

                    "/api/volunteers/email",

                    "/api/volunteers/status/*",

                    "/api/volunteers/availability/*",

                    "/api/volunteers/location",

                    "/api/volunteers/match"

                )
                .authenticated()


                /* ================= DASHBOARD ================= */

                .requestMatchers(

                    "/dashboard.html"

                )
                .authenticated()


                /* ================= CURRENT USER ================= */

                .requestMatchers(

                    "/api/me"

                )
                .authenticated()


                /* ================= EVERYTHING ELSE ================= */

                .anyRequest()
                .authenticated()
            )


            /* =================================================
               FORM LOGIN
            ================================================= */

            .formLogin(form -> form

                .loginPage(
                    "/login.html"
                )

                .loginProcessingUrl(
                    "/login"
                )

                .defaultSuccessUrl(
                    "/dashboard.html",
                    true
                )

                .permitAll()
            )


            /* =================================================
               LOGOUT
            ================================================= */

            .logout(logout -> logout

                .logoutUrl(
                    "/logout"
                )

                .logoutSuccessUrl(
                    "/"
                )

                .permitAll()
            );


        return http.build();
    }
}