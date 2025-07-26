package com.genius.primavera.application;

import org.springframework.core.io.Resource;

import java.net.MalformedURLException;

public interface AttachmentService {

    Resource getArticleAttachmentById(long id) throws MalformedURLException, NotFoundAttachment;
}
