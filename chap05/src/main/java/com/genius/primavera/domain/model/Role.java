package com.genius.primavera.domain.model;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private long id;
    private String name;
    private String description;
    private RoleType type;
}
