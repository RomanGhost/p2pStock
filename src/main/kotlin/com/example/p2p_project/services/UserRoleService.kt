package com.example.p2p_project.services

import com.example.p2p_project.models.User
import com.example.p2p_project.models.adjacent.UserRole
import com.example.p2p_project.repositories.adjacent.UserRoleRepository
import com.example.p2p_project.repositories.dataTables.RoleRepository
import org.springframework.stereotype.Service

@Service
class UserRoleService(val userRoleRepository: UserRoleRepository, val roleRepository: RoleRepository) {
    fun addUserRole(user:User, roleType:String):UserRole {
        val role = roleRepository.findByType(roleType)
        val userRole = UserRole(user = user, role= role)
        return userRoleRepository.save(userRole)
    }


}