package com.genius.primavera.infrastructure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Comment;
import com.genius.primavera.domain.model.article.CommentDto;
import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;

import org.modelmapper.ModelMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nz.net.ultraq.thymeleaf.LayoutDialect;

import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

@Configuration
public class ApplicationConfiguration implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
        var filterRegistration = new FilterRegistrationBean<XssEscapeServletFilter>();
        filterRegistration.setFilter(new XssEscapeServletFilter());
        filterRegistration.setOrder(1);
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(Article.class, ArticleDto.DetailArticle.class).addMappings(mapper -> {
            mapper.map(src -> src.getAuthorName(), ArticleDto.DetailArticle::setAuthorName);
            mapper.map(src -> src.getContents(), ArticleDto.DetailArticle::setContents);});

        modelMapper.createTypeMap(Comment.class, CommentDto.Detail.class).addMappings(mapper -> {
            mapper.map(src -> src.getAuthor().getNickname(), CommentDto.Detail::setAuthorName);
            mapper.map(src -> src.getAuthor().getConnection().getImageUrl(), CommentDto.Detail::setAuthorImage);});

        return modelMapper;
    }
}
