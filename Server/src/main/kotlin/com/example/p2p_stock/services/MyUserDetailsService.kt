package com.example.p2p_stock.services

import com.example.p2p_stock.configs.MyUserDetails
import com.example.p2p_stock.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class MyUserDetailsService(
    private val userRepository: UserRepository
): UserDetailsService {
    override fun loadUserByUsername(username: String): MyUserDetails {
        val user = userRepository.findByEmail(username)
        return MyUserDetails(user.get())
    }
}