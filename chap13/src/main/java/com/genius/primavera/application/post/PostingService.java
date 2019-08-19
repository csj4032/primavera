package com.genius.primavera.application.post;

import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostingService {

    Post save(PostDto.RequestForSave requestForSave);

    Page<Post> findAll(Pageable pageable);

    Post findById(long id);

}
