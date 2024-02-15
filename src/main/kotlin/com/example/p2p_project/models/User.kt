package com.example.p2p_project.models

import jakarta.persistence.*

@Entity
@Table(name = "app_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=null,

    @Column(unique = true, nullable = true)
    val login: String="",
    @Column(unique = false, nullable = true)
    val phone: String?="",
    @Column(unique = false, nullable = true)
    val email: String?="",

    var password: String="",
    @Column(name = "last_name", nullable = true)
    val lastName: String?="",
    @Column(name = "first_name", nullable = true)
    val firstName: String?="",
    @Column(nullable = true)
    val patronymic: String?="",
)