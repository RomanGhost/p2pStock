package com.example.p2p_stock.dataclasses

import com.example.p2p_stock.models.Cryptocurrency
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderInfo(
    val id:Long,
    val userLogin:String,
    val walletId:Long,
    val cryptocurrencyCode:String,
    val cardId:Long,
    val typeName: String,
    val statusName: String,
    val unitPrice: BigDecimal,
    val quantity: Double,
    val description: String,
    val createdAt: String,
    var lastStatusChange:String
)

data class CreateOrderInfo(
    val walletId:Long,
    val cardId:Long,
    val typeName: String,
    val statusName: String,
    val unitPrice: BigDecimal,
    val quantity: Double,
    val description: String
)


