package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

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

	private List<Subscriber> subscribers = new ArrayList<>();
}
