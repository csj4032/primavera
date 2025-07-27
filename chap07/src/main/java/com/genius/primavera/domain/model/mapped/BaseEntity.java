package com.genius.primavera.domain.model.mapped;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public class BaseEntity {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "NAME")
	private String name;
	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;
}
