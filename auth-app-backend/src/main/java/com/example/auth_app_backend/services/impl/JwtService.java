package com.example.auth_app_backend.services.impl;

import com.example.auth_app_backend.auth.entities.Role;
import com.example.auth_app_backend.auth.entities.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import java.util.UUID;


@Service
@Setter
@Getter
public class JwtService{
    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long rereshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer
    )
    {
        if(secret == null || secret.length() < 64)
        {
            throw new IllegalArgumentException("Invalid secret");
        }
        this.key= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds=accessTtlSeconds;
        this.refreshTtlSeconds=rereshTtlSeconds;
        this.issuer=issuer;
    }

    //generate token
    public String generateAccessToken(Users user) {
        Instant now = Instant.now();

        // Use the mapped roles list, not the raw entity objects
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .header().add("typ", "JWT").and() // Set header type
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                // FIX: Explicitly define ChronoUnit.SECONDS
                .expiration(Date.from(now.plus(accessTtlSeconds, ChronoUnit.SECONDS)))
                .claim("email", user.getEmail())
                .claim("roles", roles) // Use the list of Strings here
                .claim("typ", "access")
                .signWith(key, Jwts.SIG.HS512) // Modern JJWT syntax
                .compact();
    }


    //generate refreshtoken
    public String generateRefreshToken(Users user,String jti)
    {
        Instant now=Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTtlSeconds,ChronoUnit.SECONDS)))
                .claim("typ","refresh")
                .signWith(key,Jwts.SIG.HS512)
                .compact();
    }

    //parse the token
    public Jws<Claims> parse(String token)
    {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public boolean isAccessToken(String token)
    {
        Claims c=parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token)
    {
        Claims c=parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token)
    {
        Claims c=parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    public String getJti(String token) {
        return parse(token).getPayload().getId();
    }

    public List<String> getRoles(String token) {
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmail(String token) {
        Claims c = parse(token).getPayload();
        return (String) c.get("email");
    }


}
