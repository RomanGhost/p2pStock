package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.OrderType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderTypeRepository : JpaRepository<OrderType, String>

