package com.genius.primavera.interfaces;

import com.genius.primavera.application.post.PostService;
import com.genius.primavera.domain.model.post.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	public String listForPageable(Model model, Pageable pagination) {
		Page<Post> page = postService.findForPageable(pagination);
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
	public String save(Post post) {
		postService.save(post);
		return "redirect:/posts";
	}
}