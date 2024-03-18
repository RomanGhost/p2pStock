package com.example.p2p_project.repositories.dataTables

import com.example.p2p_project.models.dataTables.RequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RequestStatusRepository:JpaRepository<RequestStatus, Long> {
    fun findByName(name:String):RequestStatus
}