package com.brigi.backend.web;

import com.brigi.backend.domain.User;
import com.brigi.backend.repo.UserRepo;
import com.brigi.backend.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
  private final UserRepo users; private final PasswordEncoder enc; private final JwtService jwt;
  public AuthController(UserRepo users, PasswordEncoder enc, JwtService jwt){
    this.users = users; this.enc = enc; this.jwt = jwt;
  }

  public record Signup(String email, String password){}
  public record Login(String email, String password){}

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public void signup(@RequestBody @Valid Signup s){
    if (users.existsByEmail(s.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "email exists");
    users.save(new User(s.email(), enc.encode(s.password())));
  }

  @PostMapping("/login")
  public Map<String,String> login(@RequestBody @Valid Login l){
    var u = users.findByEmail(l.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    if (!enc.matches(l.password(), u.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return Map.of("token", jwt.issue(u.getId(), u.getEmail()));
  }
}
