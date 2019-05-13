package com.genius.primavera.application.post;

import com.genius.primavera.domain.model.post.Post;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

	@Override
	public List<Post> findAll() {
		return Collections.EMPTY_LIST;
	}
}
