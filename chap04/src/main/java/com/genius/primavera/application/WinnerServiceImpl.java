package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.Winner;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WinnerServiceImpl implements WinnerService {

	private final WinnerMapper winnerMapper;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int save(Winner winner) {
		return winnerMapper.insertWinner(winner);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int saveAndNew(Winner winner1, Winner winner2, Winner winner3, WinnerService winnerService) {
		winnerMapper.insertWinner(winner1);
		winnerService.saveRequiresNew(winner2);
		winnerMapper.insertWinner(winner3);
		return 0;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int saveAndNested(Winner winner1, Winner winner2, Winner winner3, WinnerService winnerService) {
		winnerMapper.insertWinner(winner1);
		winnerService.saveNested(winner2);
		winnerMapper.insertWinner(winner3);
		return 0;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int saveAndNotSupported(Winner winner1, Winner winner2, Winner winner3, WinnerService winnerService) {
		winnerMapper.insertWinner(winner1);
		winnerService.saveNotSupported(winner2);
		winnerMapper.insertWinner(winner3);
		return 0;
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public int saveNotSupported(Winner winner) {
		return winnerMapper.insertWinner(winner);
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public int saveNested(Winner winner) {
		return winnerMapper.insertWinner(winner);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int saveRequiresNew(Winner winner) {
		return winnerMapper.insertWinner(winner);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int saveAll(List<Winner> winners) {
		for (int i = 0; i < winners.size(); i++) {
			winnerMapper.insertWinner(winners.get(i));
		}
		return winners.size();
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public int saveAllNested(List<Winner> winners) {
		for (int i = 0; i < winners.size(); i++) {
			winnerMapper.insertWinner(winners.get(i));
		}
		return winners.size();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int innerSave(List<Winner> winners) {
		for (int i = 0; i < winners.size(); i++) {
			this.save(winners.get(i));
		}
		return winners.size();
	}

	@Override

	@Transactional(propagation = Propagation.REQUIRED)
	public int innerSaveNew(List<Winner> winners) {
		for (int i = 0; i < winners.size(); i++) {
			this.saveRequiresNew(winners.get(i));
		}
		return winners.size();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int innerNotSupported(List<Winner> winners) {
		for (int i = 0; i < winners.size(); i++) {
			this.saveNotSupported(winners.get(i));
		}
		return winners.size();
	}

	@Override
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public List<Winner> findAllUncommitted() {
		return winnerMapper.findAll();
	}

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public List<Winner> findAllCommitted() {
		return winnerMapper.findAll();
	}

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public Winner findAllByIdReadCommitted(int id) {
		return winnerMapper.findById(id);
	}

	@Override
	@Transactional(propagation = Propagation.NESTED, isolation = Isolation.REPEATABLE_READ)
	public Winner findAllByIdRepeatableRead(int id) {
		return winnerMapper.findById(id);
	}
}
