package org.burgas.kotlinspring.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CustomKafkaConsumer {

    @KafkaListener(topics = ["identity-topic"], groupId = "identity-client-id")
    fun listenToIdentityFullResponse(consumerRecord: ConsumerRecord<String, IdentityFullResponse>) {
        println("${consumerRecord.topic()} :: ${consumerRecord.value()}")
    }
}