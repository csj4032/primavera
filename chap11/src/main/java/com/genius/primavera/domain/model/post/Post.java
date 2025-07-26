package com.genius.primavera.domain.model.post;

import com.genius.primavera.domain.model.user.User;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Post {
	private long id;
	private User writer;
	private String subject;
	private String contents;
	private PostStatus status;
	private Instant regDt;
	private Instant modDt;
}