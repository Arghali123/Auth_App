package com.example.auth_app_backend.repositories;

import com.example.auth_app_backend.auth.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
}
