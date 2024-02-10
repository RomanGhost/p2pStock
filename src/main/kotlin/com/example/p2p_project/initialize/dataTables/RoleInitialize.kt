package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.Role
import com.example.p2p_project.repositories.dataTables.RoleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("roleInitialize")
class RoleInitialize(val roleRepository: RoleRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (roleRepository.count() != 0L) return;
        val roles: MutableList<Role> = mutableListOf()

        roles.add(Role(null, 3, "Пользователь"))
        roles.add(Role(null, 2, "Менеджер"))
        roles.add(Role(null, 1, "Администратор"))

        for (role in roles) {
            roleRepository.save(role)
        }
    }
}