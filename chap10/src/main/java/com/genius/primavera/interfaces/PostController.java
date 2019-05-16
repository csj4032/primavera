package com.genius.primavera.interfaces;

import com.genius.primavera.application.post.PostService;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;

import org.springframework.beans.factory.annotation.Autowired;
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
public class PostController {

	@Autowired
	private PostService postService;

	@GetMapping("/posts/all")
	public String listAll(Model model) {
		model.addAttribute("posts", postService.findAll());
		return "post/list";
	}

	@GetMapping("/posts")
	public String listForPageable(Model model, PageRequest pageRequest) {
		Paged<Post> page = postService.findForPageable(pageRequest);
		model.addAttribute("page", page);
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