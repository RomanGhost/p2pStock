    package com.example.p2p_project.models

    import jakarta.persistence.*

    @Entity
    @Table(name = "app_user")
    data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long=0,

        @Column(unique = true)
        val login: String="",

        @Column
        var password: String="",
    )