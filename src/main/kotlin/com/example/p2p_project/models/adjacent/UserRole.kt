package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.dataTables.Role
import com.example.p2p_project.models.User
import jakarta.persistence.*

@Entity
data class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "role_id")
    val role: Role
)