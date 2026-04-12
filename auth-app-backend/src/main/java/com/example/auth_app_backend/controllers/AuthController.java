package com.example.auth_app_backend.controllers;

import com.example.auth_app_backend.auth.entities.RefreshToken;
import com.example.auth_app_backend.auth.entities.Users;
import com.example.auth_app_backend.auth.payload.LoginRequest;
import com.example.auth_app_backend.auth.payload.TokenResponse;
import com.example.auth_app_backend.auth.payload.UserDTO;
import com.example.auth_app_backend.repositories.RefreshTokenRepository;
import com.example.auth_app_backend.repositories.UserRepository;
import com.example.auth_app_backend.services.AuthService;
import com.example.auth_app_backend.services.impl.CookieService;
import com.example.auth_app_backend.services.impl.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response)
    {
        //authenticate
        Authentication authenticate=authenticate(loginRequest);
        Users user=userRepository.findByEmail(loginRequest.email()).orElseThrow(()->new BadCredentialsException("Invalid Username or Password"));
        if(!user.isEnable())
        {
            throw new DisabledException("User is disabled");
        }

        String jti= UUID.randomUUID().toString();
        var refreshTokenOb= RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        //refresh token sace-- information
        refreshTokenRepository.save(refreshTokenOb);


        //generate token
        String accessToken=jwtService.generateAccessToken(user);
        String refreshToken=jwtService.generateRefreshToken(user,refreshTokenOb.getJti());

        //use cookie service to attach refresh token in cookie
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getAccessTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse=TokenResponse.of(accessToken,refreshToken,jwtService.getAccessTtlSeconds(),mapper.map(user,UserDTO.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try{
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        }catch (Exception e)
        {
            throw new BadCredentialsException("Invalid User or Password");
        }
    }

    //register user
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDTO));
    }
}
