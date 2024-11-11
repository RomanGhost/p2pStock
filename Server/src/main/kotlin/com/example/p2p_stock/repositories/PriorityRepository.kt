package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Priority
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PriorityRepository : JpaRepository<Priority, String>