package com.miljuanat.skillpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Lower bounds (percent) of each mastery level; below developingMin is NOVICE. */
@ConfigurationProperties(prefix = "skillpulse.mastery")
public record MasteryProperties(
        @DefaultValue("50") double developingMin,
        @DefaultValue("70") double proficientMin,
        @DefaultValue("85") double masteredMin) {}
