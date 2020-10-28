package com.genius.primavera.domain.relation.oneToMany;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "CUSTOMER")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Customer {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@OneToMany
	@JoinColumn(name = "CUSTOMER_ID")
	private List<Contact> contacts = new ArrayList<>();

	@Override
	public String toString() {
		return "Customer{" +
				"id=" + id +
				", name='" + name + '\'' +
				", contacts=" + contacts +
				'}';
	}
}
