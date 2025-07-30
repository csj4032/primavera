package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.Temp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TempRepository extends MongoRepository<Temp, Long> {

	Optional<Temp> findById(long id);

	List<Temp> findByIdIn(List<Long> ids);

}
