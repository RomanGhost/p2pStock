package com.example.p2p_stock.services

import com.example.p2p_stock.models.OrderStatus
import com.example.p2p_stock.repositories.OrderStatusRepository
import org.springframework.stereotype.Service

@Service
class OrderStatusService(private val orderStatusRepository: OrderStatusRepository) {

    fun findAll(): List<OrderStatus> = orderStatusRepository.findAll()

    fun save(orderStatus: OrderStatus): OrderStatus = orderStatusRepository.save(orderStatus)
}