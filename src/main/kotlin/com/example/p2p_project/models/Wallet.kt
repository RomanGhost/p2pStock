package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.Cryptocurrency
import jakarta.persistence.*

@Entity
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cryptocurrency_id")
    val cryptocurrency: Cryptocurrency = Cryptocurrency(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User = User(),

    @Column(name = "balance", nullable = false, columnDefinition = "float(2) default '0.0'")
    var balance: Double = 0.0,

    @Column(name = "name")
    var name: String = "",
    @Column(name = "public_key", unique = true)
    var publicKey: String = "",
    @Column(name = "private_key")
    val privateKey: String = ""
)
