package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    val priority: Int,
    @Column
    val type: String
)