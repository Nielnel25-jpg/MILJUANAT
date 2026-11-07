package com.csit321g4.miljuanat.skillpulse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends User {
    public Admin() { super(Role.ADMIN); }
}
