package com.example.p2p_project.repositories

import com.example.p2p_project.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>{
    fun findByLogin(login:String):User?
}
