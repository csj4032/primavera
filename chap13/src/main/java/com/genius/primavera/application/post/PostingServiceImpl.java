package com.genius.primavera.application.post;

import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.domain.repository.post.PostRepository;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostingServiceImpl implements PostingService {

    private final PostRepository postRepository;

    @Override
    public Post save(PostDto.RequestForSave requestForSave) {
        return postRepository.save(new ModelMapper().map(requestForSave, Post.class));
    }

    @Override
    public Page<Post> findBySubjectLike(String keyword, Pageable pageable) {
        return postRepository.findBySubjectLike(keyword, pageable);
    }

    @Override
    public Post findById(long id) {
        return postRepository.findById(id).orElse(new Post());
    }
}