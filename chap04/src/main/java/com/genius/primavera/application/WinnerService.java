package com.genius.primavera.application;

import com.genius.primavera.domain.model.Winner;

import java.util.List;

public interface WinnerService {

    int save(Winner winner);

    int saveAll(List<Winner> winners);

    int saveAllNotSupported(List<Winner> winners);

    int saveAllNested(List<Winner> winner1);

    List<Winner> findAllUncommitted();

    List<Winner> findAllCommitted();

    Winner findAllByIdReadCommitted(int id);

    Winner findAllByIdRepeatableRead(int id);
}
