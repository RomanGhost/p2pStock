package com.example.p2p_project.repositories.adjacent

import com.example.p2p_project.models.adjacent.UserRole
import com.example.p2p_project.models.dataTables.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRoleRepository : JpaRepository<UserRole, Long>{
    @Query("SELECT r FROM UserRole ur JOIN Role r ON ur.role.id = r.id WHERE ur.user.id = :userId")
    fun findRoleByUserId(userId:Long):List<Role>
}
