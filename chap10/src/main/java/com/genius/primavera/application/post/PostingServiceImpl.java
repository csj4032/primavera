package com.genius.primavera.application.post;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.mapper.PostMapper;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostingServiceImpl implements PostingService {

    @Autowired
    private PostMapper postMapper;

    @Override
    public int save(PostDto.RequestForSave requestForSave) {
        var post = new ModelMapper().map(requestForSave, Post.class);
        return postMapper.save(post);
    }

    @Override
    public List<Post> findAll() {
        return postMapper.findAll();
    }

    @Override
    public Paged<PostDto.ResponseForList> findForPageable(PageRequest pageRequest) {
        return new Paged<>(pageRequest, new ModelMapper().map(postMapper.findForPageable(pageRequest), new TypeToken<List<PostDto.ResponseForList>>() {
        }.getType()), postMapper.findAllCount());
    }

    @Override
    public Post findById(long id) {
        return postMapper.findById(id);
    }
}