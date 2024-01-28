package com.example.p2p_project.repositories.adjacent

import com.example.p2p_project.models.adjacent.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRoleRepository : JpaRepository<UserRole, Long>
