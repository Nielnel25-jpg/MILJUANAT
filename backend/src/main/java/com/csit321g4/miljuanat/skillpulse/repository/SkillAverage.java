package com.csit321g4.miljuanat.skillpulse.repository;

/** Class-level average mastery for one skill. */
public interface SkillAverage {
    Long getSkillId();
    String getSkillName();
    Double getAvgMastery();
    Long getStudentCount();
}
