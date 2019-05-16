package com.genius.primavera.application.post;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.mapper.PostMapper;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Override
    public int save(PostDto.RequestForSave requestForSave) {
        Post post = new ModelMapper().map(requestForSave, Post.class);
        return postMapper.save(post);
    }

    @Override
    public List<Post> findAll() {
        return postMapper.findAll();
    }

    @Override
    public Paged<Post> findForPageable(PageRequest pageRequest) {
        int postSize = postMapper.findAllCount();
        List<Post> posts = postMapper.findForPageable(pageRequest);
        return new Paged(pageRequest, posts, postSize);
    }

    @Override
    public Post findById(long id) {
        return postMapper.findById(id);
    }
}