package com.genius.primavera.domain.model.hierarchy;

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
@DiscriminatorValue("B")
public class Email extends Contact {
	@Column(name = "SIGN")
	private String sign;
	@Column(name = "DOMAIN")
	private String domain;
}