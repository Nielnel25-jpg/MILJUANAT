package com.miljuanat.skillpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillpulse.jwt")
public record JwtProperties(String secret, long expirationMs) {}
