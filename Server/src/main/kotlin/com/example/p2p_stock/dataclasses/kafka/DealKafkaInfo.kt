package com.example.p2p_stock.dataclasses.kafka


data class DealKafkaInfo(
    val id:Long,
    val buyOrder: OrderKafkaInfo,
    val sellOrder: OrderKafkaInfo,
    val statusName: String,
    val createdAt: String,
    val lastStatusChange: String
)
