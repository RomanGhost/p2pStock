package com.example.p2p_stock.services

import com.example.p2p_stock.models.OrderType
import com.example.p2p_stock.repositories.OrderTypeRepository
import org.springframework.stereotype.Service

@Service
class OrderTypeService(private val orderTypeRepository: OrderTypeRepository) {

    fun findAll(): List<OrderType> = orderTypeRepository.findAll()

    fun save(orderType: OrderType): OrderType = orderTypeRepository.save(orderType)
}