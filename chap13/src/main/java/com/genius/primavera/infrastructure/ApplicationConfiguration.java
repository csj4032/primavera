package com.genius.primavera.infrastructure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Comment;
import com.genius.primavera.domain.model.article.CommentDto;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.domain.model.user.User;
import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.convention.NamingConventions;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableJpaAuditing
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
		modelMapper.getConfiguration()
				.setSourceNameTokenizer(NameTokenizers.CAMEL_CASE)
				.setDestinationNameTokenizer(NameTokenizers.CAMEL_CASE);

		modelMapper.createTypeMap(Article.class, ArticleDto.DetailArticle.class).addMappings(mapper -> {
			mapper.map(Article::getAuthorName, ArticleDto.DetailArticle::setAuthorName);
			mapper.map(Article::getContents, ArticleDto.DetailArticle::setContents);
		});

		modelMapper.createTypeMap(Comment.class, CommentDto.Detail.class).addMappings(mapper -> {
			mapper.map(src -> src.getAuthor().getNickname(), CommentDto.Detail::setAuthorName);
			mapper.map(src -> src.getAuthor().getConnection().getImageUrl(), CommentDto.Detail::setAuthorImage);
		});

		modelMapper.createTypeMap(PostDto.RequestForSave.class, Post.class).addMappings(mapping -> {
			mapping.<Long>map(src -> src.getWriterId(), (dest, v) -> dest.getWriter().setId(v));
		});

		return modelMapper;
	}
}
