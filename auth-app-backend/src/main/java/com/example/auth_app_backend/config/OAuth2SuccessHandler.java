package com.example.auth_app_backend.config;

import com.example.auth_app_backend.auth.entities.Provider;
import com.example.auth_app_backend.auth.entities.RefreshToken;
import com.example.auth_app_backend.auth.entities.Users;
import com.example.auth_app_backend.repositories.RefreshTokenRepository;
import com.example.auth_app_backend.repositories.UserRepository;
import com.example.auth_app_backend.services.impl.CookieService;
import com.example.auth_app_backend.services.impl.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl;

    //use this url to go to google login page
    /*
     localhost:8082/oauth2/authorization/google
    */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("Successful authentication");
        logger.info(authentication.toString());

        OAuth2User oAuth2User=(OAuth2User) authentication.getPrincipal();

        //identify user:
        String registrationId="unknown";

        if(authentication instanceof OAuth2AuthenticationToken token)
        {
            registrationId=token.getAuthorizedClientRegistrationId();
        }

        logger.info("RegistrationId: "+registrationId);
        logger.info("user: "+oAuth2User.getAttributes().toString());

        Users user;
        switch (registrationId)
        {
            case "google" ->{
                String googleId=oAuth2User.getAttributes().getOrDefault("sub","").toString();
                String email=oAuth2User.getAttributes().getOrDefault("email","").toString();
                String name=oAuth2User.getAttributes().getOrDefault("name","").toString();
                String picture=oAuth2User.getAttributes().getOrDefault("picture","").toString();
                Users newUser=Users.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .enable(true)
                        .provider(Provider.GOOGLE)
                        .providerId(googleId)
                        .build();
                user=userRepository.findByEmail(email).orElseGet(()->userRepository.save(newUser));
            }
            case "github"->
            {
                String name=oAuth2User.getAttributes().getOrDefault("login","").toString();
                String githubId=oAuth2User.getAttributes().getOrDefault("id","").toString();
                String image=oAuth2User.getAttributes().getOrDefault("avatar_url","").toString();

                String email=(String)oAuth2User.getAttributes().get("email");
                if(email == null)
                {
                    email =name+"@github.com";
                }

                Users newUser=Users.builder()
                        .email(email)
                        .name(name)
                        .image(image)
                        .enable(true)
                        .provider(Provider.GITHUB)
                        .providerId(githubId)
                        .build();
                user=userRepository.findByEmail(email).orElseGet(()->userRepository.save(newUser));
            }

            default -> {
                throw new RuntimeException("Invalid registration id");
            }
        }

        String jti= UUID.randomUUID().toString();
        RefreshToken refreshTokenOb=RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(refreshTokenOb);

        String accessToken=jwtService.generateAccessToken(user);
        String refreshToken=jwtService.generateRefreshToken(user,refreshTokenOb.getJti());

        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTtlSeconds());
        //response.getWriter().write("Login SucessFull");
        response.sendRedirect(frontEndSuccessUrl);


    }
}
