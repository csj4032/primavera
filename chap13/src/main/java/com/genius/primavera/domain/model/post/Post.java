package com.genius.primavera.domain.model.post;

import com.genius.primavera.domain.model.user.User;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "POST")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User writer;

    private String subject;
    private String contents;
    private PostStatus status;
    private Instant regDt;
    private Instant modDt;
}