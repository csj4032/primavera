package com.genius.primavera.domain.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.time.Instant;

@Setter
@Getter
@Entity(name = "HierarchyContact")
@ToString
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private Instant createdAt;
}
