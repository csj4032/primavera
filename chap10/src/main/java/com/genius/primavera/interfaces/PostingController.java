package com.genius.primavera.interfaces;

import com.genius.primavera.application.post.PostingService;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.model.post.PostDto;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class PostingController {

    private final PostingService postService;

    public PostingController(PostingService postingService) {
        this.postService = postingService;
    }

    @GetMapping("/posts")
    public String listForPageable(Model model, PageRequest pageRequest) {
        model.addAttribute("page", postService.findForPageable(pageRequest));
        return "post/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable(value = "id") long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "post/detail";
    }

    @GetMapping("/post/form")
    public String form() {
        return "post/form";
    }

    @PostMapping("/post/save")
    @PreAuthorize("#requestForSave.writerId == authentication.principal.userId")
    public String save(@Validated PostDto.RequestForSave requestForSave) {
        postService.save(requestForSave);
        return "redirect:/posts";
    }
}