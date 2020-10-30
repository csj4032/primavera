package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Setter
@Getter
@Entity
@ToString(callSuper = true)
@Table(name = "CANIDAE")
public class Canidae extends Family {

	@Column(name = "CANINI")
	private String canini;
}
