package com.genius.primavera.application.post;

import com.genius.primavera.domain.model.post.Post;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    int save(Post post);

    List<Post> findAll();

    PageImpl<Post> findForPageable(Pageable pagination);

    Post findById(long id);
}
