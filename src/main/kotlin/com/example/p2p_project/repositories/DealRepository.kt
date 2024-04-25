package com.example.p2p_project.repositories

import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.dataTables.DealStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DealRepository : JpaRepository<Deal, Long>{
    fun findByStatus(status:DealStatus):List<Deal>

    @Query("""
        SELECT d FROM Deal d 
        LEFT JOIN Request sr ON sr.id = d.sellRequest
        LEFT JOIN Request br ON sr.id = d.buyRequest
        WHERE sr.userId = :userId OR br.userId = :userId;
    """)
    fun findDealByUserId(@Param("userId") userId:Long):List<Deal>

//    @Query("""
//        SELECT u FROM User u
//        WHERE u.id IN (
//        SELECT u.id FROM User u
//        LEFT JOIN UserRole ur ON ur.user.id = u.id
//        LEFT JOIN Role r ON r.id = ur.role.id
//        WHERE r.type LIKE :roleType)
//    """)
//    fun findByRoleType(@Param("roleType") roleType: String): List<User>?
}
