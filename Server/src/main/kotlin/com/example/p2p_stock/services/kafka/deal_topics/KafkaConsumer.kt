package com.example.p2p_stock.services.kafka.deal_topics


import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer{

    @KafkaListener(topics = ["\${spring.kafka.consumer.topic-name}",], groupId = "\${spring.kafka.consumer.group-id}")
    fun listen(message: String) {
        println("Received message: $message")
    }
}
