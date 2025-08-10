package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@ToString
@Table(name = "LETTER")
@IdClass(LetterId.class)
@AllArgsConstructor
@NoArgsConstructor
public class Letter {

	enum Type {
		PAYMENT,
		DEFERRED
	}

	@Id
	@ManyToOne
	@JoinColumn(name = "SENDER_ID")

	private Sender sender;

	@Id
	@ManyToOne
	@JoinColumn(name = "RECIPIENT_ID")

	private Recipient recipient;

	private String message;
	private Type type;
}
