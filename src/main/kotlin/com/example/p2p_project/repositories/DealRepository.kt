package com.example.p2p_project.repositories

import com.example.p2p_project.models.Deal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DealRepository : JpaRepository<Deal, Long>
