package com.example.auth_app_backend.config;

import com.example.auth_app_backend.helpers.UserHelper;
import com.example.auth_app_backend.repositories.UserRepository;
import com.example.auth_app_backend.services.impl.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private Logger logger=LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        logger.info("Authorization header : {}",header);

        if(header !=null && header.startsWith("Bearer "))
        {
            //extract token and validate then authenticate create and security context ke andar set karunga

            String token=header.substring(7);
            //check for access token
            try{
                if(!jwtService.isAccessToken(token))
                {
                    filterChain.doFilter(request,response);
                    return;
                }

                Jws<Claims> parse=jwtService.parse(token);
                Claims payload=parse.getPayload();
                String userId=payload.getSubject();
                UUID userUuid= UserHelper.parseUUID(userId);

                userRepository.findById(userUuid).ifPresent(user->{
                    //check for user enable or not

                    if(user.isEnable())
                    {
                        List<GrantedAuthority> authorities=user.getRoles() == null ? List.of():user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(user.getEmail(),null,authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        //final line: to set the authentication to security context
                        if(SecurityContextHolder.getContext().getAuthentication() == null)
                        {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                });
            }catch (ExpiredJwtException e)
            {
                request.setAttribute("error","Token expired");
            }catch (Exception e)
            {
                request.setAttribute("error","Invalid Token");
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        return request.getRequestURI().startsWith("/api/v1/auth");
    }
}
