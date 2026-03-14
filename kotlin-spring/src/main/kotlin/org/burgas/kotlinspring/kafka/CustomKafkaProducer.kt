package org.burgas.kotlinspring.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class CustomKafkaProducer {

    @Qualifier(value = "identityKafkaProducer")
    private final val identityKafkaProducer: KafkaProducer<String, IdentityFullResponse>

    constructor(identityKafkaProducer: KafkaProducer<String, IdentityFullResponse>) {
        this.identityKafkaProducer = identityKafkaProducer
    }

    fun sendIdentityFullResponse(identityFullResponse: IdentityFullResponse) {
        val producerRecord: ProducerRecord<String, IdentityFullResponse> =
            ProducerRecord("identity-topic", "create-identity", identityFullResponse)
        this.identityKafkaProducer.send(producerRecord)
    }
}