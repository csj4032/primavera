package com.genius.primavera.application.post;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;

import java.util.List;

public interface PostingService {

    int save(PostDto.RequestForSave requestForSave);

    List<Post> findAll();

    Paged<PostDto.ResponseForList> findForPageable(PageRequest pageRequest, String keyword);

    Post findById(long id);
}
