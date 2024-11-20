package com.example.p2p_stock.services

import com.example.p2p_stock.models.Order
import com.example.p2p_stock.models.OrderStatus
import com.example.p2p_stock.repositories.OrderStatusRepository
import org.springframework.stereotype.Service

@Service
class OrderStatusService(private val orderStatusRepository: OrderStatusRepository) {

    fun getDefaultStatus(): OrderStatus = orderStatusRepository.findById("Модерация").get()

    fun findAll(): List<OrderStatus> = orderStatusRepository.findAll()

    fun findById(statusName:String): OrderStatus = orderStatusRepository.findById(statusName).get()

    fun save(orderStatus: OrderStatus): OrderStatus = orderStatusRepository.save(orderStatus)
}