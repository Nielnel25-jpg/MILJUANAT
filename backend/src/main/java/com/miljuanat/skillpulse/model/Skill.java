package com.miljuanat.skillpulse.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** A node in the skill taxonomy; parent/child gives the hierarchy (topic -> skill -> objective). */
@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Skill parent;

    @OneToMany(mappedBy = "parent")
    private List<Skill> children = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Skill getParent() { return parent; }
    public void setParent(Skill parent) { this.parent = parent; }
    public List<Skill> getChildren() { return children; }
}
