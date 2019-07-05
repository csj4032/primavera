package com.genius.primavera.application;

import com.genius.primavera.application.storage.StorageService;
import com.genius.primavera.domain.mapper.article.ArticleAttachmentMapper;
import com.genius.primavera.domain.model.article.Attachment;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final StorageService storageService;
    private final ArticleAttachmentMapper articleAttachmentMapper;

    @Override
    public Resource getArticleAttachmentById(long id) throws NotFoundAttachment {
        Attachment attachment = articleAttachmentMapper.findById(id);
        if(Objects.isNull(attachment)) {
            throw new NotFoundAttachment();
        }
        return storageService.loadAsResource(attachment.getName());
    }
}
