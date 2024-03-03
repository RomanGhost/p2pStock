package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.Cryptocurrency
import jakarta.persistence.*

@Entity
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "cryptocurrency_id")
    val cryptocurrency: Cryptocurrency = Cryptocurrency(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    val user: User = User(),

    val name: String = "",
    @Column(name = "public_key")
    val publicKey: String = "",
    @Column(name = "private_key")
    val privateKey: String = ""
)
