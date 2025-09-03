CREATE TABLE auth_providers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  provider VARCHAR(32) NOT NULL,
  access_token TEXT NOT NULL,
  refresh_token TEXT NOT NULL,
  expires_at DATETIME NOT NULL,
  UNIQUE KEY uq_user_provider (user_id, provider),
  CONSTRAINT fk_ap_user FOREIGN KEY (user_id) REFERENCES users(id),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  strava_activity_id BIGINT UNIQUE,
  start_time DATETIME NOT NULL,
  sport VARCHAR(40),
  distance_m INT NOT NULL,
  moving_time_s INT NOT NULL,
  elev_gain_m INT DEFAULT 0,
  avg_hr SMALLINT,
  avg_pace_s_km INT,
  raw_json JSON,
  INDEX idx_user_time (user_id, start_time),
  CONSTRAINT fk_act_user FOREIGN KEY (user_id) REFERENCES users(id)
);