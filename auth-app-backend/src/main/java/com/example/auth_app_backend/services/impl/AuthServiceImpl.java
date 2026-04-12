package com.example.auth_app_backend.services.impl;

import com.example.auth_app_backend.auth.payload.UserDTO;
import com.example.auth_app_backend.services.AuthService;
import com.example.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        //logic
        //verify email
        //verify password
        //default roles

        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        UserDTO userDTO1=userService.createUser(userDTO);
        return userDTO1;
    }
}
