package com.example.p2p_stock.dataclasses.kafka

import java.math.BigDecimal

data class OrderKafkaInfo(
    val id:Long,
    val userHashPublicKey:String,
    val cryptocurrencyCode:String,
    val typeName:String,
    val unitPrice: BigDecimal,
    val quantity:Double,
    val createdAt: String,
    val lastStatusChange: String,
)