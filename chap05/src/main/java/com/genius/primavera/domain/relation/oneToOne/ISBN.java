package com.genius.primavera.domain.relation.oneToOne;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "ISBN")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class ISBN {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "VALUE")
	private String value;

	@NonNull
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BOOK_ID")
	private Book book;

	@Override
	public String toString() {
		return "ISBN{" +
				"id=" + id +
				", value='" + value + '\'' +
				", book=" + book +
				'}';
	}
}
