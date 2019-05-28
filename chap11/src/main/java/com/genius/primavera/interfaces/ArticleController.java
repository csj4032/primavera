package com.genius.primavera.interfaces;

import com.genius.primavera.application.article.WriteArticleService;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.WriteType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class ArticleController {

    @Autowired
    private WriteArticleService writeArticleService;

    @GetMapping(value = "/articles")
    public String articles(Model model, PageRequest pageRequest) {
        model.addAttribute("page", writeArticleService.findForPageable(pageRequest));
        return "article/list";
    }

    @GetMapping(value = "/articles/{id:[0-9]+}")
    public String detail(@PathVariable(value = "id") long id, Model model) {
        model.addAttribute("article", writeArticleService.findByIdWithContent(id));
        return "article/detail";
    }

    @GetMapping(value = "/articles/{type:form|modify|reply}")
    public String form(Model model, @PathVariable(value = "type") WriteType type, @RequestParam(value = "id", required = false, defaultValue = "0") long id) {
        model.addAttribute("form", writeArticleService.findByForForm(type, id));
        return "article/form";
    }

    @PostMapping(value = "/articles/save")
    public String save(@Valid ArticleDto.WriteArticle writeArticle) {
        writeArticleService.write(writeArticle);
        return "redirect:/articles";
    }

    @PostMapping(value = "/articles/modify")
    public String modify(@Valid ArticleDto.WriteArticle writeArticle) {
        return "redirect:/articles/" + writeArticleService.modify(writeArticle).getId();
    }
}