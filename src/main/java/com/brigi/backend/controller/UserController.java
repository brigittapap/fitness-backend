package com.brigi.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;  // logging import

import com.brigi.backend.repo.UserRepo;
import com.brigi.backend.domain.User;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepo userRepository;

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
            if (auth == null) {
        log.error("Authentication object is null");
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    log.debug("Authentication type: {}", auth.getClass().getName());
        String email = auth.getName();
        log.debug("Attempting to find user with email from JWT: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail()
        );
    }
}
