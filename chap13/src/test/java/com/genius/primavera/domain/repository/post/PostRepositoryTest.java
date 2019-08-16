package com.genius.primavera.domain.repository.post;

import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostStatus;
import com.genius.primavera.domain.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Order(1)
    @Rollback(false)
    @Transactional
    @DisplayName("포스트 작성 테스트")
    public void writePostTest() {
        Post post = new Post();
        post.setWriter(userRepository.findById(1).orElseThrow());
        post.setSubject("안녕하세요");
        post.setContents("안녕하세요");
        post.setStatus(PostStatus.PUBLIC);
        postRepository.save(post);
    }
}