package com.example.p2p_project.models

import jakarta.persistence.*

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    val login: String,
    val phone: String,
    val email: String,
    val password: String,
    @Column(name = "last_name")
    val lastName: String,
    @Column(name = "first_name")
    val firstName: String,
    val patronymic: String,
    @Column(name = "is_active")
    val isActive: Boolean
)