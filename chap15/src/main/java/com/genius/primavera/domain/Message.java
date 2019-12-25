package com.genius.primavera.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;


@Getter
@Setter
@ToString
public class Message extends RepresentationModel<Message> {
	private int code;
}
