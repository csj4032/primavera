package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Winner;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WinnerMapper {

	int winnerInsert(Winner winner);
}
