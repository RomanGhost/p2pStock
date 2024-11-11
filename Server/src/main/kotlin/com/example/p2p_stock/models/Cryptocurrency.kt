package com.example.p2p_stock.models

import jakarta.persistence.*

@Entity
@Table(name = "cryptocurrencies")
data class Cryptocurrency(
    @Id
    @Column(nullable = false, length = 32)
    val name: String = "",

    @Column(nullable = false, length = 4, unique = true)
    val code: String = ""
)