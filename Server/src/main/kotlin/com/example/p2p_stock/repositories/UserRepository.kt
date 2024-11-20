package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.swing.text.StyledEditorKit.BoldAction


@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun existsByLogin(login: String): Boolean
    fun existsByEmail(email: String): Boolean
}