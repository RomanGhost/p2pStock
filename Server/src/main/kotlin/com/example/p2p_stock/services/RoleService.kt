package com.example.p2p_stock.services

import com.example.p2p_stock.models.Role
import com.example.p2p_stock.repositories.RoleRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoleService(private val roleRepository: RoleRepository) {

    fun findAll(): List<Role> = roleRepository.findAll()

    fun getUserRole(): Optional<Role> = roleRepository.findById("USER")
    fun getAdminRole(): Optional<Role> = roleRepository.findById("ADMIN")
    fun getManagerRole(): Optional<Role> = roleRepository.findById("MANAGER")

    fun save(role: Role): Role = roleRepository.save(role)
}