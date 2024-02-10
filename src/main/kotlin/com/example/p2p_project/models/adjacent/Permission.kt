package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.dataTables.Action
import com.example.p2p_project.models.dataTables.Role
import jakarta.persistence.*

@Entity
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "action_id")
    val action: Action,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "role_id")
    val role: Role
)