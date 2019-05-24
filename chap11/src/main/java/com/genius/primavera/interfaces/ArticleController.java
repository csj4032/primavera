package com.genius.primavera.interfaces;

import com.genius.primavera.application.article.WriteArticleService;
import com.genius.primavera.domain.PageRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ArticleController {

    @Autowired
    private WriteArticleService writeArticleService;

    @GetMapping(value = "/articles")
    public String articles(Model model, PageRequest pageRequest) {
        model.addAttribute("page", writeArticleService.findForPageable(pageRequest));
        return "article/list";
    }
}