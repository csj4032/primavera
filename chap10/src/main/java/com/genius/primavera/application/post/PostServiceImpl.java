package com.genius.primavera.application.post;

import com.genius.primavera.domain.mapper.PostMapper;
import com.genius.primavera.domain.model.post.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Override
    public int save(Post post) {
        return postMapper.save(post);
    }

    @Override
    public Post findById(long id) {
        return new Post();
    }

    @Override
    public List<Post> findAll() {
        return postMapper.findAll();
    }
}
