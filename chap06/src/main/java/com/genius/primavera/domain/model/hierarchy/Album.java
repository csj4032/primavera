package com.genius.primavera.domain.model.hierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@ToString(callSuper = true)
@Table(name = "ALBUM")
@DiscriminatorValue("A")
public class Album extends Item {

	public Album(String name, int price, String artist) {
		this.setName(name);
		this.setPrice(price);
		this.artist = artist;
	}

	@Column(name = "ARTIST")
	private String artist;
}
