package com.example.auth_app_backend.auth.payload;

public record TokenResponse(
        String accessToken,
        String refrehToken,
        long expiresIn,
        String tokenType,
        UserDTO user
) {
    public static TokenResponse of(String accessToken,String refrehToken,long expiresIn,UserDTO user)
    {
        return new TokenResponse(accessToken,refrehToken,expiresIn,"Bearer",user);
    }
}
