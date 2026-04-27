package com.example.auth_app_backend.config;

import com.example.auth_app_backend.dtos.ApiError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationSuccessHandler successHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,AuthenticationSuccessHandler successHandler)
    {
        this.jwtAuthenticationFilter=jwtAuthenticationFilter;
        this.successHandler=successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AppConstants.AUTH_PUBLIC_URLS).permitAll()
                        .requestMatchers(AppConstants.AUTH_ADMIN_URLS).hasRole(AppConstants.ADMIN_ROLE)
                        //.requestMatchers(AppConstants.AUTH_GUEST_URLS).hasRole(AppConstants.GUEST_ROLE)
                        .requestMatchers(HttpMethod.GET).hasRole(AppConstants.GUEST_ROLE)
                        .requestMatchers("/api/v1/users", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2.successHandler(successHandler).failureHandler(null))
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            String message = e.getMessage();
                            String error = (String) request.getAttribute("error");
                            if (error != null) message = error;
                            var apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access", message, request.getRequestURI(), true);
                            new ObjectMapper().writeValue(response.getWriter(), apiError);
                        })
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            String message = e.getMessage();
                            String error = (String) request.getAttribute("error");
                            if (error != null) message = error;
                            var apiError = ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden Access", message, request.getRequestURI(), true);
                            var objectMappper=new ObjectMapper();
                            response.getWriter().write(objectMappper.writeValueAsString(apiError));
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }
//    @Bean
//    public UserDetailsService users()
//    {
//        User.UserBuilder userBuilder=User.withDefaultPasswordEncoder();
//        UserDetails user1=userBuilder.username("daenish").password("abc").roles("ADMIN").build();
//        UserDetails user2=userBuilder.username("niraj").password("xyz").roles("ADMIN").build();
//        UserDetails user3=userBuilder.username("dinesh").password("123").roles("USER").build();
//        return new InMemoryUserDetailsManager(user1,user2,user3);
//    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.front-end-url}") String corsUrls)
    {
        String[] urls = corsUrls.trim().split(",");

        var config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(urls));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
