package com.example.p2p_project.models.dataTables

import jakarta.persistence.*

@Entity
data class OperationType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column(name = "operation_name")
    val operationName: String
)