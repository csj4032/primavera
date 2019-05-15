package com.genius.primavera.application.post;

import com.genius.primavera.domain.model.post.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    int save(Post post);

    List<Post> findAll();

    Page<Post> findForPageable(Pageable pagination);

    Post findById(long id);
}
