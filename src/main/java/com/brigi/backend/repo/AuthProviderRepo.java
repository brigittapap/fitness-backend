package com.brigi.backend.repo;

import com.brigi.backend.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthProviderRepo extends JpaRepository<AuthProvider, Long> {
  Optional<AuthProvider> findByUserIdAndProvider(Long userId, String provider);
}
