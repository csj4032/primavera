package com.genius.primavera.domain.relation.oneToMany;


import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "PLAYER")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Player {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID")
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@ManyToOne
	@JoinColumn(name = "TEAM_ID")
	// 플레이어가 팀 Prime Key 를 Foreign key 등록
	private Team team;
}
