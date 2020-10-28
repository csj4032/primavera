package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "DESTINATION")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Destination {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@ManyToMany(mappedBy = "destinations")
	private List<Origin> origins = new ArrayList<>();

	@Override
	public String toString() {
		return "Destination{" +
				"id=" + id +
				", name='" + name + '\'' +
				", origins=" + origins.stream().map(e -> e.getId().toString()).collect(Collectors.joining(",", "[", "]")) +
				'}';
	}
}
