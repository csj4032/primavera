package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.Winner;

import org.springframework.stereotype.Service;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveAll(List<Winner> winners) {
        for (int i = 0; i < winners.size(); i++) {
            winnerMapper.insertWinner(winners.get(i));
        }
        return winners.size();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int saveAllNotSupported(List<Winner> winners) {
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
}
