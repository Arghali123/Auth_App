package com.example.auth_app_backend.auth.payload;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {
    private UUID id=UUID.randomUUID();
    private String name;
}
