package org.burgas.kotlinspring.config

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JacksonJsonSerializer

@Configuration
class KafkaProducerConfig {

    @Bean
    fun producerConfig(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonJsonSerializer::class.java
        )
    }

    @Bean
    fun identityKafkaProducer(): KafkaProducer<String, IdentityFullResponse> {
        return KafkaProducer<String, IdentityFullResponse>(this.producerConfig())
    }
}