package com.example.p2p_stock.models

import jakarta.persistence.*

@Entity
@Table(name = "wallets")
data class Wallet(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long=0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User?=null,

    @Column(length = 128)
    val name: String = "",

    @Column(name = "public_key", length = 64, nullable = false)
    val publicKey: String = "",

    @Column(name = "private_key", length = 64, nullable = false)
    val privateKey: String = "",

    @ManyToOne
    @JoinColumn(name = "cryptocurrency_id", nullable = false)
    val cryptocurrency: Cryptocurrency?=null,

    @Column(nullable = false)
    val balance: Double = 0.0
)