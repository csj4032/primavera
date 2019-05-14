package com.genius.primavera.application.post;

import com.genius.primavera.domain.mapper.PostMapper;
import com.genius.primavera.domain.model.post.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    public List<Post> findAll() {
        return postMapper.findAll();
    }

    @Override
    public Page<Post> findForPageable(Pageable pageable) {
        List<Post> posts = postMapper.findForPageable(pageable);
        return new PageImpl(posts, pageable, posts.size());
    }

    @Override
    public Post findById(long id) {
        return new Post();
    }
}