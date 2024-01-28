package com.example.p2p_project.repositories.dataTables

import com.example.p2p_project.models.dataTables.RequestType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface RequestTypeRepository : JpaRepository<RequestType, Long>

