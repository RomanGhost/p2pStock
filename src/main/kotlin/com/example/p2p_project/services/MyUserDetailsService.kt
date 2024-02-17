package com.example.p2p_project.services

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.repositories.UserRepository
import com.example.p2p_project.repositories.adjacent.UserRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class MyUserDetailsService:UserDetailsService {
    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var userRoleRepository: UserRoleRepository
    override fun loadUserByUsername(username: String): MyUserDetails {
        val user = userRepository.findByLogin(username)
        println(user)
        if (user == null) {
            println("Call error")
            throw Exception("User with $username not found")
        }
        val roles = userRoleRepository.findRoleByUserId(user.id!!)
        return MyUserDetails(user, roles)
    }
}