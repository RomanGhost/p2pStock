package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*


@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun existsByLogin(login: String): Boolean
    fun existsByEmail(email: String): Boolean

    @Query("""
    SELECT u FROM User u 
    WHERE 
        (:id IS NULL OR u.id = :id) 
        AND (:isActive IS NULL OR u.isActive = :isActive) 
        AND u.email <> :adminEmail
    """)
    fun findByFilters(
        @Param("id") id: Long?,
        @Param("isActive") isActive: Boolean?,
        @Param("adminEmail") adminEmail: String,
        pageable: Pageable
    ): Page<User>


}