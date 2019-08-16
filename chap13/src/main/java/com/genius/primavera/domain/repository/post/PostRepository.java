package com.genius.primavera.domain.repository.post;

import com.genius.primavera.domain.model.post.Post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
