package com.genius.primavera.domain.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@ToString(callSuper = true)
@Table(name = "canidae")
@DiscriminatorValue("CANIDAE")
public class Canidae extends Family {

	@Column(name = "pack_size")
	private Integer packSize;

	@Column(name = "hunting_style")
	private String huntingStyle;

	public void setCanini(String canini) {
		this.huntingStyle = canini;
	}

	public String getCanini() {
		return this.huntingStyle;
	}
}
