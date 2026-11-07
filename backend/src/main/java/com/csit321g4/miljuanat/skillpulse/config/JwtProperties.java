package com.csit321g4.miljuanat.skillpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillpulse.jwt")
public record JwtProperties(String secret, long expirationMs) {}
