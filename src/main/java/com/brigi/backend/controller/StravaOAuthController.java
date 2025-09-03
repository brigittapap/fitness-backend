package com.brigi.backend.controller;

import com.brigi.backend.domain.AuthProvider;
import com.brigi.backend.domain.User;
import com.brigi.backend.repo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/oauth/strava")
@RequiredArgsConstructor
public class StravaOAuthController {

  @Value("${strava.clientId}")     private String clientId;
  @Value("${strava.clientSecret}") private String clientSecret;
  @Value("${strava.redirectUri}")  private String redirectUri;
  @Value("${strava.scope:read,activity:read_all}") private String scope;

  private final UserRepo users;
  private final AuthProviderRepo authProviders;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient http = HttpClient.newHttpClient();

  /**
   * Step 1: Redirect the logged-in user to Strava consent page.
   * Keep it authenticated: we must know who is connecting.
   */
  @GetMapping("/start")
  public ResponseEntity<Void> start(Authentication auth) {
    String url = "https://www.strava.com/oauth/authorize"
        + "?client_id=" + urlEnc(clientId)
        + "&response_type=code"
        + "&redirect_uri=" + urlEnc(redirectUri)
        + "&scope=" + urlEnc(scope)
        + "&approval_prompt=auto";

    return ResponseEntity.status(302).location(URI.create(url)).build();
  }

  /**
   * Step 2: Strava redirects back here with ?code=...
   * We exchange the code for tokens and store them for the current user.
   */
  @GetMapping("/callback")
  @Transactional
  public ResponseEntity<?> callback(@RequestParam String code, Authentication auth) throws Exception {
    // Find current user by email (JWT principal = email per your JwtAuthFilter)
    String email = auth.getName();
    User user = users.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

    // Exchange code -> tokens
    String form = "client_id=" + urlEnc(clientId)
        + "&client_secret=" + urlEnc(clientSecret)
        + "&code=" + urlEnc(code)
        + "&grant_type=authorization_code";

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create("https://www.strava.com/oauth/token"))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(form))
        .build();

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() >= 300) {
      return ResponseEntity.status(res.statusCode()).body(Map.of("error", "Token exchange failed", "body", res.body()));
    }

    JsonNode json = objectMapper.readTree(res.body());
    String access  = json.get("access_token").asText();
    String refresh = json.get("refresh_token").asText();
    long   expSec  = json.get("expires_at").asLong();  // epoch seconds

    // Upsert auth_providers row
    AuthProvider ap = authProviders.findByUserIdAndProvider(user.getId(), "STRAVA")
        .orElseGet(AuthProvider::new);

    ap.setUserId(user.getId());
    ap.setProvider("STRAVA");
    ap.setAccessToken(access);
    ap.setRefreshToken(refresh);
    ap.setExpiresAt(Timestamp.from(Instant.ofEpochSecond(expSec)));
    authProviders.save(ap);

    return ResponseEntity.ok(Map.of(
        "message", "Strava connected",
        "userId", user.getId(),
        "provider", "STRAVA",
        "expiresAt", ap.getExpiresAt().toInstant().toString()
    ));
  }

  private static String urlEnc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
