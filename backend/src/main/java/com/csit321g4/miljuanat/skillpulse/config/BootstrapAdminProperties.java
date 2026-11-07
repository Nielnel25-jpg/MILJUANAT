package com.csit321g4.miljuanat.skillpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillpulse.bootstrap-admin")
public record BootstrapAdminProperties(String email, String password, String fullName) {}
