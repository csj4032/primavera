package com.genius.primavera.domain.model.mapped;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public class BaseEntity {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "NAME")
	private String name;
	@Column(name = "REG_DT")
	private LocalDateTime regDt;
	@Column(name = "MOD_DT")
	private LocalDateTime modDt;
}
