package com.genius.primavera.domain.relation.oneToMany;

import lombok.*;

import jakarta.persistence.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity(name = "RelationContact")
@Getter
@Setter
@Table(name = "CONTACT")
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

	enum Type {
		EMAIL,
		PHONE;
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "TYPE")
	private Type type;

	@NonNull
	@Column(name = "VALUE")
	private String value;

	@ManyToOne

	@JoinColumn(name = "CUSTOMER_ID")
	private Customer customer;

	@Override
	public String toString() {
		return "Contact{" +
				"id=" + id +
				", type=" + type +
				", value='" + value + '\'' +
				", customer=" + customer.getId() +
				'}';
	}
}
