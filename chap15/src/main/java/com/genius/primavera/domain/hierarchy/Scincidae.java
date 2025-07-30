package com.genius.primavera.domain.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@ToString(callSuper = true)
@Table(name = "scincidae")
@DiscriminatorValue("SCINCIDAE")
public class Scincidae extends Family {

	@Column(name = "scale_type")
	private String scaleType;

	@Column(name = "burrowing_ability")
	private String burrowingAbility;

	public void setGenus(String genus) {
		this.scaleType = genus;
	}

	public String getGenus() {
		return this.scaleType;
	}
}
