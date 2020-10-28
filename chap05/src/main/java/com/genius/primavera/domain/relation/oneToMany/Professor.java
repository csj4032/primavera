package com.genius.primavera.domain.relation.oneToMany;

import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "PROFESSOR")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Professor {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@OneToMany
	// STUDENT 테이블의 PROFESSOR_ID
	@JoinColumn(name = "PROFESSOR_ID")
	private List<Student> students = new ArrayList<>();
}
