package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@ToString
@Table(name = "FAMILY")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Family {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "table_generator")
	@TableGenerator(name = "table_generator", table = "TABLE_SEQ", schema = "advance")
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "family_generator")
	//@SequenceGenerator(name="family_generator", sequenceName = "FAMILY_SEQ")
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "REG_DT")
	private LocalDateTime regDt;

	@Column(name = "MOD_DT")
	private LocalDateTime modDt;
}
