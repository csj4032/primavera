package com.genius.primavera.domain.hierarchy;


import lombok.*;

import jakarta.persistence.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contact_mobiles")
public class Mobile extends Contact {
	@Column(name = "phone_number")
	private String phoneNumber;
	
	@Column(name = "carrier")
	private String carrier;
	
	public void setProvider(String provider) {
		this.carrier = provider;
	}
	
	public String getProvider() {
		return carrier;
	}
	
	public void setNumber(String number) {
		this.phoneNumber = number;
	}
	
	public String getNumber() {
		return phoneNumber;
	}
}
