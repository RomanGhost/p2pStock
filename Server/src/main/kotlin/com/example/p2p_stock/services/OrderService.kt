package com.example.p2p_stock.services

import com.example.p2p_stock.models.Order
import com.example.p2p_stock.repositories.OrderRepository
import org.springframework.stereotype.Service

@Service
class OrderService(private val orderRepository: OrderRepository) {

    fun findByUserId(userId: Long): List<Order> = orderRepository.findByUserId(userId)

    fun save(order: Order): Order = orderRepository.save(order)

    fun delete(id: Long) = orderRepository.deleteById(id)
}