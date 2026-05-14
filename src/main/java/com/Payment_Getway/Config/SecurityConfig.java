package com.Payment_Getway.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.web.SecurityFilterChain;

import com.Payment_Getway.Repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {

        return username -> userRepository.findByEmail(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .disabled(!"ACTIVE".equalsIgnoreCase(user.getAccountStatus()))
                        .build()
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + username)
                );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

            .csrf(csrf -> csrf
                    .ignoringRequestMatchers(
                            "/h2-console/**",
                            "/webhook"
                    )
            )

            // Enable frames for H2 Console
            .headers(headers ->
                    headers.frameOptions(frame -> frame.disable())
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/",
                        "/auth/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/products/**",

                        "/h2-console/**"

                ).permitAll()

                .anyRequest().authenticated()
            )

            .formLogin(form -> form

                .loginPage("/auth/login")

                .loginProcessingUrl("/login")

                .defaultSuccessUrl("/dashboard", true)

                .permitAll()
            )

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/auth/login")

                .permitAll()
            );

        return http.build();
    }
}
