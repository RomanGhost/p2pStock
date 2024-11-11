package com.example.p2p_stock.models

import jakarta.persistence.*

@Entity
@Table(name = "banks")
data class Bank(
    @Id
    @Column(length = 32)
    val name: String = ""
)

@Entity
@Table(name = "cards")
data class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "card_number", length = 16, nullable = false)
    val cardNumber: String = "",

    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    val bank: Bank,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
)
