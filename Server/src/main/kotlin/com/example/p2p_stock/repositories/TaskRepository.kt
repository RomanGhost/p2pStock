package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    fun findByDealId(dealId: Long): List<Task>
    fun findByManagerId(managerId: Long): List<Task>
}