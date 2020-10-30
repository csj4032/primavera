package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Setter
@Getter
@Entity
@ToString(callSuper = true)
@Table(name = "SCINCIDAE")
public class Scincidae extends Family {

	@Column(name = "GENUS")
	private String genus;
}
