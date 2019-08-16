package com.genius.primavera.application.post;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.domain.repository.post.PostRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostingServiceImpl implements PostingService {

    @Autowired
    private PostRepository postRepository;

    @Override
    public Post save(PostDto.RequestForSave requestForSave) {
        var post = new ModelMapper().map(requestForSave, Post.class);
        return postRepository.save(post);
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Paged<PostDto.ResponseForList> findForPageable(PageRequest pageRequest, String keyword) {
        return null;
    }

    @Override
    public Post findById(long id) {
        return postRepository.findById(id).orElse(new Post());
    }
}