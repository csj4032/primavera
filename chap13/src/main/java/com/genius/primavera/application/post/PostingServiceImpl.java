package com.genius.primavera.application.post;

import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.domain.repository.post.PostRepository;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostingServiceImpl implements PostingService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Post save(PostDto.RequestForSave requestForSave) {
        // ToDo ModelMapper 설정 확인
        var post = modelMapper.map(requestForSave, Post.class);
        return postRepository.save(post);
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Override
    public Post findById(long id) {
        return postRepository.findById(id).orElseThrow();
    }
}