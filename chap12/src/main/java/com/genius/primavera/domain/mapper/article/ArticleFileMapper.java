package com.genius.primavera.domain.mapper.article;

import com.genius.primavera.domain.model.article.File;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleFileMapper {

    int save(File[] files);
}
