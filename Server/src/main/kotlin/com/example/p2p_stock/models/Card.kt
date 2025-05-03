package com.example.p2p_stock.models

import jakarta.persistence.*

@Entity
@Table(name = "banks")
data class Bank(
    @Id
    @Column(length = 32)
    val name: String = "",

)

@Entity
@Table(name = "cards")
data class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long=0,

    @Column(name = "card_name", length = 256, nullable = false)
    val cardName: String = "",

    @Column(name = "card_number", length = 16, nullable = false, unique = true)
    val cardNumber: String = "",

    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    val bank: Bank?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User?=null,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean=false
)
