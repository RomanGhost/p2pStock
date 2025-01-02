package com.example.p2p_stock.services.kafka.deal_topics

import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.dataclasses.kafka.DealKafkaInfo
import com.example.p2p_stock.dataclasses.kafka.OrderKafkaInfo
import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.models.Order
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
    private fun orderToOrderKafkaInfo(order: Order):OrderKafkaInfo{
        return OrderKafkaInfo(
            id=order.id,
            userHashPublicKey = order.wallet?.publicKey!!,
            cryptocurrencyCode = order.wallet?.cryptocurrency?.name!!,
            typeName = order.type?.name!!,
            unitPrice = order.unitPrice,
            quantity = order.quantity,
            createdAt = order.createdAt.toString(),
            lastStatusChange = order.lastStatusChange.toString(),

        )
    }

    fun sendMessage(deal: Deal) {
        val buyOrder = orderToOrderKafkaInfo(deal.buyOrder!!)
        val sellOrder = orderToOrderKafkaInfo(deal.sellOrder!!)

        val dealKafkaInfo = DealKafkaInfo(
            id = deal.id,
            buyOrder = buyOrder,
            sellOrder = sellOrder,
            statusName = deal.status?.name!!,
            createdAt =  deal.createdAt.toString(),
            lastStatusChange = deal.lastStatusChange.toString(),
        )

        val messageData = objectMapper.writeValueAsString(dealKafkaInfo)
        kafkaTemplate.send(topic, messageData)
        println("Message sent to topic '$topic': $messageData")
    }
}
