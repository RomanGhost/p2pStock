package com.example.p2p_project.repositories.dataTables

import com.example.p2p_project.models.dataTables.DealStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DealStatusRepository : JpaRepository<DealStatus, Long>{
    fun findByName(dealName:String):DealStatus
}
