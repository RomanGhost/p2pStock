package com.example.p2p_project.repositories

import com.example.p2p_project.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>{
    fun findByLogin(login:String):User?

    @Query("""
        SELECT u FROM User u 
        WHERE u.id IN (
        SELECT u.id FROM User u 
        LEFT JOIN UserRole ur ON ur.user.id = u.id
        LEFT JOIN Role r ON r.id = ur.role.id
        WHERE r.type LIKE :roleType)
    """)
    fun findByRoleType(@Param("roleType") roleType: String): List<User>?

    @Query("""
        SELECT u FROM User u 
        WHERE u.id NOT IN (
        SELECT u.id FROM User u 
        LEFT JOIN UserRole ur ON ur.user.id = u.id
        LEFT JOIN Role r ON r.id = ur.role.id
        WHERE r.type LIKE :roleType)
    """)
    fun findWithoutRole(@Param("roleType") roleType: String): List<User>?
}
