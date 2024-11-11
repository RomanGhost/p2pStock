package com.example.p2p_stock.models

import jakarta.persistence.*

@Entity
@Table(name = "wallet_cryptocurrency_balances")
data class WalletCryptocurrencyBalance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    val wallet: Wallet,

    @ManyToOne
    @JoinColumn(name = "cryptocurrency_id", nullable = false)
    val cryptocurrency: Cryptocurrency,

    @Column(nullable = false)
    val balance: Double = 0.0
)