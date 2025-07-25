package com.genius.primavera.domain.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@ToString(callSuper = true)
@Table(name = "felidae")
@DiscriminatorValue("FELIDAE")
public class Felidae extends Family {

	@Column(name = "climbing_ability")
	private String climbingAbility;

	@Column(name = "hunting_style")
	private String huntingStyle;
	
	public void setSystem(String system) {
		this.climbingAbility = system;
	}
	
	public String getSystem() {
		return climbingAbility;
	}
}
