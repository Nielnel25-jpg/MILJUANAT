package com.csit321g4.miljuanat.skillpulse.repository;

/** Per-skill weighted totals for one student: earned vs. possible (both already multiplied by mapping weight). */
public interface SkillAggregate {
    Long getSkillId();
    Double getEarned();
    Double getPossible();
}
