package com.genius.primavera.interfaces;

import com.genius.primavera.application.post.PostingService;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.infrastructure.aspect.PrimaveraLogging;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PostingController {

    private final PostingService postService;

    @PrimaveraLogging(type = "PostingController")
    @GetMapping("/posts")
    public String listForPageable(Model model, Pageable pageable, @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword) {
        model.addAttribute("page", postService.findAll(pageable, keyword));
        return "post/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable(value = "id") long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "post/detail";
    }

    @GetMapping("/posts/form")
    public String form() {
        return "post/form";
    }

    @PostMapping("/posts/save")
    @PreAuthorize("#requestForSave.writerId == authentication.principal.userId")
    public String save(@Validated PostDto.RequestForSave requestForSave) {
        postService.save(requestForSave);
        return "redirect:/posts";
    }
}