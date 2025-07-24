package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Setter
@Getter
@Entity
@ToString(callSuper = true)
@DiscriminatorValue("A")
public class Address extends Contact {
	@Column(name = "COUNTRY")
	private String country;
	@Column(name = "CITY")
	private String city;
	@Column(name = "STREET")
	private String street;
	@Column(name = "ZIPCODE")
	private String zipCode;
}
