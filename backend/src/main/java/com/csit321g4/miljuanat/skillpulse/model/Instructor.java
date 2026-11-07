package com.csit321g4.miljuanat.skillpulse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "instructors")
public class Instructor extends User {

    private String department;

    public Instructor() { super(Role.INSTRUCTOR); }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
