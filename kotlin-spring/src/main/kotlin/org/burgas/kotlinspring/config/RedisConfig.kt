package org.burgas.kotlinspring.config

import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.burgas.kotlinspring.entity.note.NoteFullResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.ObjectMapper

@Configuration
class RedisConfig {

    @Bean
    fun identityRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, IdentityFullResponse> {
        val template = RedisTemplate<String, IdentityFullResponse>()
        template.connectionFactory = connectionFactory

        val mapper = ObjectMapper()

        val serializer = JacksonJsonRedisSerializer(mapper, IdentityFullResponse::class.java)

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = serializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = serializer

        return template
    }

    @Bean
    fun noteRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, NoteFullResponse> {
        val template = RedisTemplate<String, NoteFullResponse>()
        template.connectionFactory = connectionFactory

        val mapper = ObjectMapper()

        val serializer = JacksonJsonRedisSerializer(mapper, NoteFullResponse::class.java)

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = serializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = serializer

        return template
    }
}