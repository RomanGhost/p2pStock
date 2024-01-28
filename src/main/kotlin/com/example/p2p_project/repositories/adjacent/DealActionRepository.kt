package com.example.p2p_project.repositories.adjacent

import com.example.p2p_project.models.adjacent.DealAction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DealActionRepository : JpaRepository<DealAction, Long>
