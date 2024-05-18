package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column
    val priority: Int = -1,
    @Column
    val type: String = ""
)