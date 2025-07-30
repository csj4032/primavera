package com.genius.primavera.domain.model.article;

import com.genius.primavera.domain.model.BaseEntity;

import java.io.File;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ARTICLE_ATTACHMENT")
public class Attachment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "ARTICLE_ID")
	private long articleId;

	@Column(name = "NAME")
	private String name;

	@Column(name = "SIZE")
	private long size;

	@Column(name = "PATH")
	private String path;

	@Transient
	private File file;

	public boolean isExists() {
		return this.id != 0;
	}

	public static class AttachmentBuilder {
		private String name;
		private long size;
		private String path;
		private File file;

		public AttachmentBuilder file(File file) {
			this.file = file;
			this.name = file.getName();
			this.path = file.getAbsolutePath();
			this.size = file.length();
			return this;
		}
	}
}