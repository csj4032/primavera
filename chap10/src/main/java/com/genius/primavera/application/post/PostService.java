package com.genius.primavera.application.post;

import com.genius.primavera.domain.model.post.Post;

import java.util.List;

public interface PostService {

    List<Post> findAll();

    int save(Post post);

    Post findById(long id);
}
