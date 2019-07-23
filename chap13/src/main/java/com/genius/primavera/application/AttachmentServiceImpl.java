package com.genius.primavera.application;

import com.genius.primavera.application.storage.StorageService;
import com.genius.primavera.domain.model.article.Attachment;
import com.genius.primavera.domain.repository.AttachmentRepository;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final StorageService storageService;
    private final AttachmentRepository attachmentRepository;

    @Override
    public Resource getArticleAttachmentById(long id) throws NotFoundAttachment {
        Attachment attachment = attachmentRepository.findById(id).orElseThrow(NotFoundAttachment::new);
        return storageService.loadAsResource(attachment.getName());
    }
}