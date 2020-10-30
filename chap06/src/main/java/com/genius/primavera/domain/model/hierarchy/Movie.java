package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@ToString
@Table(name = "MOVIE")
@DiscriminatorValue("C")
public class Movie extends Item {
}
