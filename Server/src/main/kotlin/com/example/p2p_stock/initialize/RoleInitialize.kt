package com.example.p2p_stock.initialize

import com.example.p2p_stock.models.Role
import com.example.p2p_stock.repositories.RoleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("roleInitialize")
class RoleInitialize(val roleRepository: RoleRepository) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val roles = listOf(
            Role(name = "USER"),
            Role(name = "MANAGER"),
            Role(name = "ADMIN")
        )

        val existingRoles = roleRepository.findAll()

        for (role in roles) {
            val exists = existingRoles.any { it.name.equals(role.name, ignoreCase = true) }
            if (!exists) {
                roleRepository.save(role)
            }
        }
    }
}
