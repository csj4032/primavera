package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Setter
@Getter
@Entity
@ToString
@Table(name = "BOOK")
@DiscriminatorValue("B")
public class Book extends Item {
	private String author;
	private String isbn;
}
