package com.example.auth_app_backend.services;

import com.example.auth_app_backend.auth.payload.UserDTO;

public interface AuthService {
    UserDTO registerUser(UserDTO userDTO);
}
