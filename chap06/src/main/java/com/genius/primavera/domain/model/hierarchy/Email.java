package com.genius.primavera.domain.model.hierarchy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "EMAIL")
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("B")
public class Email extends Contact {
	@Column(name = "SIGN")
	private String sign;
	@Column(name = "DOMAIN")
	private String domain;
}