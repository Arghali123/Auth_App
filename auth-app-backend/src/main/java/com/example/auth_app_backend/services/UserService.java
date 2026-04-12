package com.example.auth_app_backend.services;

import com.example.auth_app_backend.auth.payload.UserDTO;

public interface UserService {

    //create user
    UserDTO createUser(UserDTO userDTO);

    //get user by email
    UserDTO getUserByEmail(String email);

    //update user
    UserDTO updateUser(UserDTO userDTO,String userId);

    //delete user
    void deleteUser(String userId);

    //get user by id
    UserDTO getUserById(String userId);

    //get all users
    Iterable<UserDTO> getAllUsers();
}
