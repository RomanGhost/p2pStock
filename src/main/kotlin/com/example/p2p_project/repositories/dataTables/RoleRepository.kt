package com.example.p2p_project.repositories.dataTables

import com.example.p2p_project.models.dataTables.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long>{
    fun findByType(type:String):Role
}