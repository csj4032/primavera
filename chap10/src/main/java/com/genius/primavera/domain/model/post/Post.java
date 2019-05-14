package com.genius.primavera.domain.model.post;

import com.genius.primavera.domain.model.user.User;
import lombok.*;

import java.time.LocalDateTime;

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
	private LocalDateTime regDt = LocalDateTime.now();
	private LocalDateTime modDt = LocalDateTime.now();
}