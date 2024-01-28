package com.example.p2p_project.repositories.dataTables

import com.example.p2p_project.models.dataTables.OperationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OperationTypeRepository : JpaRepository<OperationType, Long>
