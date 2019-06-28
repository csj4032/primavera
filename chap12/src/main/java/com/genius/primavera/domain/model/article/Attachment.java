package com.genius.primavera.domain.model.article;

import lombok.*;

import java.io.File;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
	private long id;
	private Article article;
	private File file;

	public boolean isExists() {
		return this.id != 0;
	}

	public long getArticleId() {
		return article.getId();
	}

	public String getName() {
		return file.getName();
	}

	public String getPath() {
		return file.getAbsolutePath();
	}

	public long getSize() {
		return file.length();
	}
}