package com.brigi.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "auth_providers",
       uniqueConstraints = @UniqueConstraint(name="uq_user_provider", columnNames = {"user_id","provider"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AuthProvider {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, length = 32)
  private String provider;   // "STRAVA"

  @Column(name = "access_token", nullable = false, length = 255)
  private String accessToken;

  @Column(name = "refresh_token", nullable = false, length = 255)
  private String refreshToken;

  @Column(name = "expires_at", nullable = false)
  private Timestamp expiresAt;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Timestamp createdAt;
}
