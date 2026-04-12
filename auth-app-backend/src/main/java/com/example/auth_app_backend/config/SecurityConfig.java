package com.example.auth_app_backend.config;

import com.example.auth_app_backend.dtos.ApiError;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
                        .cors(Customizer.withDefaults())
                                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequests->
                authorizeHttpRequests.requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/users").permitAll()
                        .anyRequest().authenticated()
        )
                .exceptionHandling(ex->ex.authenticationEntryPoint((request, response, e) -> {
                    //error message
                    //e.printStackTrace();
                    response.setStatus(401);
                    response.setContentType("application/json");
                    String message=e.getMessage();
                    String error=(String) request.getAttribute("error");
                    if(error != null)
                    {
                        message=error;
                    }
                    //Map<String,String> errorMap=Map.of("message",message,"status",String.valueOf(401),"statusCode",Integer.toString(401));
                    var apiError= ApiError.of(HttpStatus.UNAUTHORIZED.value(),"Unauthorized access",message,request.getRequestURI(),true);
                    var objectMapper=new ObjectMapper();
                    response.getWriter().write(objectMapper.writeValueAsString(apiError));
                }))
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
}
