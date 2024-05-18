package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class DealStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column
    var name: String = "",

    @Column
    val priority: Int = -1
)