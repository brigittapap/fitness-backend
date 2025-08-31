package com.brigi.backend.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity @Table(name="users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true)
  private String email;

  @Column(name="password_hash", nullable=false)
  private String passwordHash;

  @Column(name="created_at", insertable=false, updatable=false)
  private Timestamp createdAt;

  public User() {}
  public User(String email, String passwordHash){ this.email = email; this.passwordHash = passwordHash; }

  public Long getId(){ return id; }
  public String getEmail(){ return email; }
  public void setEmail(String email){ this.email = email; }
  public String getPasswordHash(){ return passwordHash; }
  public void setPasswordHash(String passwordHash){ this.passwordHash = passwordHash; }
  public Timestamp getCreatedAt(){ return createdAt; }
}
