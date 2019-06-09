package com.genius.primavera.application;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class HelloServiceImpl implements HelloService {

	@Override
	public String getArticle(int id) {
		return "";
	}

	@Override
	public List<String> getArticles() {
		return Collections.EMPTY_LIST;
	}
}