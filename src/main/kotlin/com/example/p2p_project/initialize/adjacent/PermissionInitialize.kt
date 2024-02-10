package com.example.p2p_project.initialize.adjacent

import com.example.p2p_project.models.adjacent.Permission
import com.example.p2p_project.models.dataTables.Action
import com.example.p2p_project.models.dataTables.Role
import com.example.p2p_project.repositories.adjacent.PermissionRepository
import com.example.p2p_project.repositories.dataTables.ActionRepository
import com.example.p2p_project.repositories.dataTables.RoleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@DependsOn("actionInitialize", "roleInitialize")
class PermissionInitialize(
    val permissionRepository:PermissionRepository,
    val actionRepository:ActionRepository,
    val roleRepository: RoleRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (permissionRepository.count() != 0L) return

        // Выдача прав пользователю
        var action:Action = actionRepository.getReferenceById(4L)
        var role: Role = roleRepository.getReferenceById(1L)
        val userPermission = Permission(null,action, role) // role, action
        permissionRepository.save(userPermission)

        // Выдача прав администратора
        action = actionRepository.getReferenceById(1)
        role = roleRepository.getReferenceById(3)
        val adminPermission = Permission(null,action, role) // role, action
        permissionRepository.save(adminPermission)

        // Выдача прав менеджеру
        role = roleRepository.getReferenceById(2)
        for(i in 1..3L) {
            action = actionRepository.getReferenceById(i)
            
        }
    }
}