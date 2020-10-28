package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import javax.persistence.*;

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
	//LETTER 테이블의 SENDER 테이블 외래키
	private Sender sender;

	@Id
	@ManyToOne
	@JoinColumn(name = "RECIPIENT_ID")
	//LETTER 테이블의 RECIPIENT 테이블 외래키
	private Recipient recipient;

	private String message;
	private Type type;
}
