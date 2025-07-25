package com.genius.primavera.domain.hierarchy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contact_emails")
public class Email extends Contact {
	@Column(name = "email")
	private String emailAddress;
	
	@Column(name = "domain")
	private String domain;
	
	@Column(name = "is_verified")
	private Boolean isVerified = false;
	
	public void setSign(String sign) {
		this.emailAddress = sign + "@" + (domain != null ? domain : "example.com");
	}
	
	public String getSign() {
		if (emailAddress != null && emailAddress.contains("@")) {
			return emailAddress.split("@")[0];
		}
		return "";
	}
}