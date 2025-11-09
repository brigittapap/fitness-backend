package com.brigi.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  public JwtAuthFilter(JwtService jwt){ this.jwt = jwt; }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String authHeader = req.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      try {
        String token = authHeader.substring(7);
        Claims claims = jwt.parse(token).getBody();
        String email = claims.getSubject();

        
      if (email != null && !email.trim().isEmpty()) {
                    var auth = new UsernamePasswordAuthenticationToken(
                        email, 
                        null, 
                        List.of()  // Consider adding proper authorities here
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Successfully authenticated user: {}", email);
                } else {
                    log.warn("JWT token has no subject (email)");
                }
      } catch (Exception ignored) { 
          log.error("Failed to process JWT token", ignored);
          SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(req, res);
  }
}
