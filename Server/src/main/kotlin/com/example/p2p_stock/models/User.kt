package com.example.p2p_stock.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long=0,

    @Column(nullable = false, unique = true, length = 128)
    val login: String = "",

    @Column(nullable = false, length = 256)
    val password: String = "",

    @Column(nullable = false, unique = true, length = 256)
    val email: String = "",

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "role", referencedColumnName = "name")
    var role: Role? = null
)
