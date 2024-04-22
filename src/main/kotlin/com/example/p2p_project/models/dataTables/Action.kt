package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class Action(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    val name: String,

    @Column
    val description: String
)