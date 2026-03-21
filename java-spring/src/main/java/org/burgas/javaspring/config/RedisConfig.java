package org.burgas.javaspring.config;

import org.burgas.javaspring.dto.identity.IdentityFullResponse;
import org.burgas.javaspring.dto.note.NoteFullResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, IdentityFullResponse> identityRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, IdentityFullResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(IdentityFullResponse.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, NoteFullResponse> noteRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, NoteFullResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(NoteFullResponse.class));
        return template;
    }
}
