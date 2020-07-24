package com.genius.primavera.infrastructure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Comment;
import com.genius.primavera.domain.model.article.CommentDto;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.infrastructure.serializer.KryoRedisSerializer;
import com.genius.primavera.infrastructure.serializer.KryoSerializer;
import com.genius.primavera.infrastructure.serializer.SnappyRedisSerializer;
import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;

import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.convention.NamingConventions;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

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
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new SnappyRedisSerializer(new KryoRedisSerializer()));
		return redisTemplate;
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(getRedisStandaloneConfiguration(), getLettucePoolingClientConfigurationBuilder());
	}

	private RedisStandaloneConfiguration getRedisStandaloneConfiguration() {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
		configuration.setHostName("localhost");
		configuration.setPort(6379);
		return configuration;
	}

	private LettuceClientConfiguration getLettucePoolingClientConfigurationBuilder() {
		return LettucePoolingClientConfiguration.builder()
				.poolConfig(genericObjectPoolConfig())
				.commandTimeout(Duration.ofMillis(300))
				.shutdownTimeout(Duration.ofMillis(500))
				.clientResources(DefaultClientResources.create()).build();
	}

	private GenericObjectPoolConfig genericObjectPoolConfig() {
		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setMinIdle(5);
		genericObjectPoolConfig.setMaxIdle(10);
		genericObjectPoolConfig.setMaxTotal(10);
		genericObjectPoolConfig.setMaxWaitMillis(100);
		genericObjectPoolConfig.setTestOnBorrow(true);
		genericObjectPoolConfig.setTestOnReturn(true);
		genericObjectPoolConfig.setTestWhileIdle(true);
		return genericObjectPoolConfig;
	}
}