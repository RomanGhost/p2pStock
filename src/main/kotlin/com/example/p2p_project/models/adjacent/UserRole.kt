package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.User
import com.example.p2p_project.models.dataTables.Role
import jakarta.persistence.*

@Entity
data class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "role_id")
    val role: Role
)