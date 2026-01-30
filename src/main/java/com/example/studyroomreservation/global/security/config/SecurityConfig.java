package com.example.studyroomreservation.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. 공용 리소스 및 Swagger 관련 경로 허용
                        .requestMatchers(
                                "/", "/login", "/public/**", "/session-expired",
                                "/swagger-ui/**",    // Swagger UI 접속용
                                "/v3/api-docs/**",   // OpenAPI Spec(JSON) 데이터용
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 2. API 테스트를 위해 /api로 시작하는 모든 경로 임시 허용
                        .requestMatchers("/api/**").permitAll()

                        // 3. 권한별 접근 제어
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 4. CSRF 설정: API와 Swagger 경로에서 POST/PUT 테스트가 가능하도록 예외 처리
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/swagger-ui/**", "/v3/api-docs/**")
                )

                .formLogin(form -> form
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/session-expired")
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // 인메모리 사용자 설정 (테스트용)
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}1234")
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}1234")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}