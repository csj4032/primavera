package com.genius.primavera.domain.relation.oneToMany;


import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
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
	//@JoinColumn(name = "CUSTOMER_ID", insertable = false, updatable = false)
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
