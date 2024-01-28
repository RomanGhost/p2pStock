package com.example.p2p_project.repositories.dataTables

import com.example.p2p_project.models.dataTables.Action
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ActionRepository : JpaRepository<Action, Long>