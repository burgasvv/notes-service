package org.burgas.kotlinspring.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer

@Configuration
class KafkaConsumerConfig {

    @Bean
    fun identityKafkaConsumer(): KafkaConsumer<String, IdentityFullResponse> {
        val properties: Map<String, Any> = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.CLIENT_ID_CONFIG to "identity-client-id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JacksonJsonDeserializer::class.java,
            JacksonJsonDeserializer.TYPE_MAPPINGS to
                    "org.burgas.kotlinspring.entity.identity.IdentityFullResponse:org.burgas.kotlinspring.entity.identity.IdentityFullResponse"
        )
        return KafkaConsumer<String, IdentityFullResponse>(properties)
    }
}