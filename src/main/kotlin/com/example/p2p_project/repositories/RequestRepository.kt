package com.example.p2p_project.repositories

import com.example.p2p_project.models.Request
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RequestRepository : JpaRepository<Request, Long>{
    fun findByUserId(userId:Long):List<Request>
}
