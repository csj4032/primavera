package com.genius.primavera.domain.model.hierarchy;


import lombok.*;

import jakarta.persistence.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("C")
//@PrimaryKeyJoinColumn
public class Mobile extends Contact {
	@Column(name = "PROVIDER")
	private String provider;
	@Column(name = "NUMBER")
	private String number;
}
