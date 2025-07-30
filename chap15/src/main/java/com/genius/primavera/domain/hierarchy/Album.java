package com.genius.primavera.domain.hierarchy;

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
@DiscriminatorValue("ALBUM")
public class Album extends Item {

	public Album() {}

	public Album(String name, int price, String artist) {
		this.setName(name);
		this.setPrice(price);
		this.artist = artist;
	}

	@Column(name = "artist")
	private String artist;

	@Column(name = "etc")
	private String etc;
}
