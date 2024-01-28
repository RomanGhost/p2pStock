package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class Cryptocurrency(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    val name: String,
    val code: String
)