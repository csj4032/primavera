package com.genius.primavera.domain.relation.oneToOne;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "BOOK")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Book {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "TITLE")
	private String title;

	@OneToOne(mappedBy = "book", fetch = FetchType.LAZY)
	public ISBN isbn;

	@Override
	public String toString() {
		return "Book{" +
				"id=" + id +
				", title='" + title + '\'' +
				", isbn=" + isbn.getId() +
				'}';
	}
}
