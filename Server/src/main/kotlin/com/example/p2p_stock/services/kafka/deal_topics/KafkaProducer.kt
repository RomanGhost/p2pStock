package com.example.p2p_stock.services.kafka.deal_topics

import com.example.p2p_stock.dataclasses.DealInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${spring.kafka.producer.topic-name}") private val topic: String,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {

    fun sendMessage(dataObject: DealInfo) {
        val messageData = objectMapper.writeValueAsString(dataObject)
        kafkaTemplate.send(topic, messageData)
        println("Message sent to topic '$topic': $messageData")
    }
}
