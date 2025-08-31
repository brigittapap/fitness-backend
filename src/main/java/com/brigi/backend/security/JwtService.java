package com.brigi.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService {
  private final byte[] key;
  private final long ttlMinutes;

  public JwtService(@Value("${app.jwtSecret}") String secret,
                    @Value("${app.jwtTtlMinutes:43200}") long ttlMinutes) {
    this.key = secret.getBytes(StandardCharsets.UTF_8);
    this.ttlMinutes = ttlMinutes;
  }

  public String issue(Long userId, String email){
    var now = Instant.now();
    return Jwts.builder()
      .setSubject(String.valueOf(userId))
      .claim("email", email)
      .setIssuedAt(Date.from(now))
      .setExpiration(Date.from(now.plus(ttlMinutes, ChronoUnit.MINUTES)))
      .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256)
      .compact();
  }

  public Jws<Claims> parse(String token){
    return Jwts.parserBuilder()
      .setSigningKey(Keys.hmacShaKeyFor(key))
      .build()
      .parseClaimsJws(token);
  }
}
