package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@ToString
@Table(name = "PUBLISHER")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Publisher {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@ManyToMany
	@JoinTable(
			name = "PUBLISHER_SUBSCRIBER",
			joinColumns = @JoinColumn(name = "PUBLISHER_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_PUBLISHER_SUBSCRIBER_PUBLISHER_ID")),
			inverseJoinColumns = @JoinColumn(name = "SUBSCRIBER_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_PUBLISHER_SUBSCRIBER_SUBSCRIBER_ID"))
	)
	//JoinColumn (PUBLISHER_SUBSCRIBER 테이블 조인 컬럼 : PUBLISHER_ID) referencedColumnName PUBLISHER_SUBSCRIBER 에서 사용할 외래키의 PUBLISHER 테이블의 컬럼명
	//inverseJoinColumns (SUBSCRIBER 테이블 조인 컬럼 : SUBSCRIBER_ID) referencedColumnName PUBLISHER_SUBSCRIBER 에서 사용할 외래키의 SUBSCRIBER 테이블의 컬럼명
	private List<Subscriber> subscribers = new ArrayList<>();
}
