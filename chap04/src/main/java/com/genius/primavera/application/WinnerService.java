package com.genius.primavera.application;

import com.genius.primavera.domain.model.Winner;

import java.util.List;

public interface WinnerService {

	int save(Winner winner);

	int saveAndNew(Winner winner1, Winner winner2, Winner winner3, WinnerService winnerService);

	int saveAndNested(Winner winner1, Winner winner2, Winner winner3, WinnerService winnerService);

	int saveNested(Winner winner);

	int saveRequiresNew(Winner winner);

	int saveNotSupported(Winner winner);

	int saveAll(List<Winner> winners);

	int saveAndNotSupported(Winner winner1, Winner winner2, Winner winner3, WinnerService winnerService);

	int saveAllNested(List<Winner> winner);

	int innerSave(List<Winner> winners);

	int innerSaveNew(List<Winner> winners);

	int innerNotSupported(List<Winner> winners);

	List<Winner> findAllUncommitted();

	List<Winner> findAllCommitted();

	Winner findAllByIdReadCommitted(int id);

	Winner findAllByIdRepeatableRead(int id);
}
