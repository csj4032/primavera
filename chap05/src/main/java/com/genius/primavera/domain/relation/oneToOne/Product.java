package com.genius.primavera.domain.relation.oneToOne;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "PRODUCT")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Product {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@NonNull
	@OneToOne
	@JoinColumn(name = "PRODUCT_ID")
	private Serial serial;

	@Override
	public String toString() {
		return "Product{" +
				"id=" + id +
				", name='" + name + '\'' +
				", serial= {id=" + serial.getId() + ", number=" + serial.getNumber() + ", type=" + serial.getType() + "}" +
				'}';
	}
}
