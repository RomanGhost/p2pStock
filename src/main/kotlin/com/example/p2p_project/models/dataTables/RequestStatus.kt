package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class RequestStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column
    val name: String="",

    @Column
    val priority: Int = -1
)
