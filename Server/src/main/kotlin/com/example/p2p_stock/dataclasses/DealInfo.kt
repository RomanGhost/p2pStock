package com.example.p2p_stock.dataclasses

import com.example.p2p_stock.models.Order

data class CreateDealInfo (
    val walletId: Long,
    val cardId: Long,
    val counterpartyOrderId: Long
)

data class DealInfo(
    val id:Long,
    val buyOrder: OrderInfo,
    val sellOrder: OrderInfo,
    val statusName: String,
    val createdAt: String,
    val lastStatusChange: String
)
