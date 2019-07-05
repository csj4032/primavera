package com.genius.primavera.interfaces;

import com.genius.primavera.application.AttachmentService;
import com.genius.primavera.application.NotFoundAttachment;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/attachment/{id}")
    public ResponseEntity<Resource> getAttachmentById(@PathVariable("id") long id, HttpServletRequest request) throws MalformedURLException, NotFoundAttachment {
        Resource resource = attachmentService.getArticleAttachmentById(id);
        String contentType = request.getServletContext().getMimeType(resource.getFilename());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}