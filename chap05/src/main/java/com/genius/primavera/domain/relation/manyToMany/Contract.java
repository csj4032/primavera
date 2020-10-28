package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "CONTRACT")
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "BUY_ID")
	private Buyer buyer;

	@ManyToOne
	@JoinColumn(name = "SELLER_ID")
	private Seller seller;

	@Column(name = "REGISTERED_AT")
	LocalDateTime registeredAt;
}
