package com.genius.primavera.infrastructure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genius.primavera.domain.model.Temp;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Comment;
import com.genius.primavera.domain.model.article.CommentDto;
import com.genius.primavera.infrastructure.serializer.KryoRedisSerializer;
import com.genius.primavera.infrastructure.serializer.SnappyRedisSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
@EnableJpaAuditing
public class ApplicationConfiguration implements WebMvcConfigurer {


    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public ModelMapper modelMapper() {
        var modelMapper = new ModelMapper();
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

        return modelMapper;
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new SnappyRedisSerializer<>(new KryoRedisSerializer<>()));
        redisTemplate.setHashValueSerializer(new SnappyRedisSerializer<>(new KryoRedisSerializer<>()));
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean(name = "tempRedisTemplate")
    public RedisTemplate<String, Temp> tempRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, Temp>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new SnappyRedisSerializer<>(new KryoRedisSerializer<>()));
        redisTemplate.setHashValueSerializer(new SnappyRedisSerializer<>(new KryoRedisSerializer<>()));
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    @Profile("!test")
    public RedisConnectionFactory redisConnectionFactory() {
        var redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("localhost");
        redisConfig.setPort(6380);
        
        var clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(createGenericObjectPoolConfig())
                .commandTimeout(Duration.ofSeconds(30))
                .shutdownTimeout(Duration.ofSeconds(5))
                .clientOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .build())
                .clientResources(DefaultClientResources.builder()
                        .ioThreadPoolSize(4)
                        .computationThreadPoolSize(4)
                        .build())
                .build();
                
        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    private GenericObjectPoolConfig<Object> createGenericObjectPoolConfig() {
        var poolConfig = new GenericObjectPoolConfig<Object>();
        poolConfig.setMinIdle(2);
        poolConfig.setMaxIdle(8);
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxWaitMillis(2000);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
}