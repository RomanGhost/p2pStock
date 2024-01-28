package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
@Table(name="error")
data class ErrorModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    val name: String,
    val priority: Int
)