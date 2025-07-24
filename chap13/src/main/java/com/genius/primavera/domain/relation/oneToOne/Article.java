package com.genius.primavera.domain.relation.oneToOne;

import lombok.*;

import jakarta.persistence.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "ARTICLE")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Article {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "SUBJECT")
	private String subject;

	@NonNull
	@OneToOne
	@JoinColumn(name = "CONTENT_ID", referencedColumnName = "ID")
	private Content content;
}
