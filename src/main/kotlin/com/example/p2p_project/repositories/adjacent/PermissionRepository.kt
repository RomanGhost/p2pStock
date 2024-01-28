package com.example.p2p_project.repositories.adjacent

import com.example.p2p_project.models.adjacent.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, Long>
