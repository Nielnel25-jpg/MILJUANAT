package com.csit321g4.miljuanat.skillpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SkillPulseApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillPulseApplication.class, args);
    }
}
