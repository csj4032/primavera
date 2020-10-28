package com.genius.primavera.domain.relation.manyToMany;

import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "ORIGIN")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Origin {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "NAME")
	private String name;

	@ManyToMany
	@JoinTable(
			name = "ORIGIN_DESTINATION",
			joinColumns = @JoinColumn(name = "ORIGIN_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_ORIGIN_DESTINATION_ORIGIN_ID")),
			inverseJoinColumns = @JoinColumn(name = "DESTINATION_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_ORIGIN_DESTINATION_DESTINATION_ID"))
	)
	private List<Destination> destinations = new ArrayList<>();

	@Override
	public String toString() {
		return "Origin{" +
				"id=" + id +
				", name='" + name + '\'' +
				", destinations=" + destinations +
				'}';
	}
}
