package com.example.p2p_project.models

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    val wallet: Wallet,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "card_id")
    val card: Card,

    val type: Int,
    @Column(name = "price_per_unit")
    val pricePerUnit: BigDecimal,
    val quantity: Float,
    val description: String,
    @Column(name = "date_time")
    val dateTime: LocalDateTime
)