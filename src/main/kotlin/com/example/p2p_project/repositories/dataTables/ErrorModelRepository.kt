package com.example.p2p_project.repositories.dataTables

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.example.p2p_project.models.dataTables.ErrorModel


@Repository
interface ErrorModelRepository : JpaRepository<ErrorModel, Long>
