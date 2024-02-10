package com.example.p2p_project.models

import jakarta.persistence.*

@Entity
@Table(name = "app_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column(unique = true)
    val login: String,
    @Column(unique = true)
    val phone: String?,
    @Column(unique = true)
    val email: String?,

    var password: String,
    @Column(name = "last_name")
    val lastName: String?,
    @Column(name = "first_name")
    val firstName: String?,
    val patronymic: String?,
    @Column(name = "is_active")
    val isActive: Boolean?
)