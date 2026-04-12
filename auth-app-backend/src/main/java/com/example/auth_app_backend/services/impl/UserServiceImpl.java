package com.example.auth_app_backend.services.impl;

import com.example.auth_app_backend.auth.entities.Provider;
import com.example.auth_app_backend.auth.entities.Users;
import com.example.auth_app_backend.auth.payload.UserDTO;
import com.example.auth_app_backend.exceptions.ResourceNotFoundException;
import com.example.auth_app_backend.helpers.UserHelper;
import com.example.auth_app_backend.repositories.UserRepository;
import com.example.auth_app_backend.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if(userDTO.getEmail() == null || userDTO.getEmail().isBlank())
        {
            throw new IllegalArgumentException("Email is required");
        }

        if(userRepository.existsByEmail(userDTO.getEmail()))
        {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        //if you have extra check __put here__

        Users user=modelMapper.map(userDTO,Users.class);
        user.setProvider(userDTO.getProvider()!=null ? userDTO.getProvider(): Provider.LOCAL);
        //role assign here to user__ for authorization.
        //TODO
        Users savedUser=userRepository.save(user);
        return modelMapper.map(savedUser,UserDTO.class);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        Users user=userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with given id"));
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO, String userId) {
        UUID uuid=UserHelper.parseUUID(userId);
        Users existingUser=userRepository
                .findById(uuid)
                .orElseThrow(()->new ResourceNotFoundException("User not found by given id"));
        if(userDTO.getName()!=null) existingUser.setName(userDTO.getName());
        if(userDTO.getImage()!=null) existingUser.setImage(userDTO.getImage());
        if(userDTO.getProvider()!=null) existingUser.setProvider(userDTO.getProvider());

        //TODO:change password updation logic
        if(userDTO.getPassword()!=null) existingUser.setPassword(userDTO.getPassword());
        existingUser.setEnable(userDTO.isEnable());
        existingUser.setUpdatedAt(Instant.now());
        Users updatedUser=userRepository.save(existingUser);
        return modelMapper.map(updatedUser,UserDTO.class);

    }

    @Override
    public void deleteUser(String userId) {
        UUID uId= UserHelper.parseUUID(userId);
        Users user=userRepository.findById(uId).orElseThrow(()->new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public UserDTO getUserById(String userId) {
        Users user=userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(()->new ResourceNotFoundException("User not found with given id"));
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    @Transactional
    public Iterable<UserDTO> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user-> modelMapper.map(user,UserDTO.class))
                .toList();
    }
}
