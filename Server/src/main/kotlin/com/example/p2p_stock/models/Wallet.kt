package com.example.p2p_stock.models

import jakarta.persistence.*

@Entity
@Table(name = "wallets")
data class Wallet(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(length = 128)
    val name: String = "",

    @Column(name = "public_key", length = 64, nullable = false)
    val publicKey: String = "",

    @Column(name = "private_key", length = 64, nullable = false)
    val privateKey: String = "",

    @OneToMany(mappedBy = "wallet", cascade = [CascadeType.ALL], orphanRemoval = true)
    val balances: Set<WalletCryptocurrencyBalance> = emptySet()
)