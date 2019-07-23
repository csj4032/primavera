package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.article.Attachment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
