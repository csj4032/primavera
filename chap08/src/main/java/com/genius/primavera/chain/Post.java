package com.genius.primavera.chain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
	private String to;
	private String from;
	private String message;
	private int step;
}