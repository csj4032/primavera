package com.genius.primavera.application;

import com.genius.primavera.domain.model.Winner;

import java.util.List;

public interface WinnerService {

    int save(Winner winner);

    int saveNew(Winner winner);

    int saveNotSupported(Winner winner);

    int saveAll(List<Winner> winners);

    int saveAllNotSupported(List<Winner> winners);

    int saveAllNested(List<Winner> winner);

    int innerSave(List<Winner> winners);

    int innerSaveNew(List<Winner> winners);

    int innerNotSupported(List<Winner> winners);

    List<Winner> findAllUncommitted();

    List<Winner> findAllCommitted();

    Winner findAllByIdReadCommitted(int id);

    Winner findAllByIdRepeatableRead(int id);
}
