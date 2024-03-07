package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class RequestType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0L,

    @Column
    val name: String? = ""
)
