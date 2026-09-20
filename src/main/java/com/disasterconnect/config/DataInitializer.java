package com.disasterconnect.config;

import com.disasterconnect.entity.User;
import com.disasterconnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            /*
             * ============================================
             * INITIAL ADMIN ACCOUNT
             * ============================================
             *
             * Creates the first administrator only when
             * no ADMIN account exists.
             */

            boolean adminExists =
                    userRepository.findAll()
                            .stream()
                            .anyMatch(user ->
                                    "ADMIN".equalsIgnoreCase(
                                            user.getRole()
                                    )
                            );


            if (!adminExists) {

                User admin = new User(
                        "DisasterConnect Admin",
                        "admin@disasterconnect.com",
                        passwordEncoder.encode(
                                "Admin@12345"
                        ),
                        "ADMIN"
                );

                userRepository.save(admin);

                System.out.println(
                        "========================================"
                );

                System.out.println(
                        "DisasterConnect ADMIN account created."
                );

                System.out.println(
                        "Email: admin@disasterconnect.com"
                );

                System.out.println(
                        "========================================"
                );
            }
        };
    }
}