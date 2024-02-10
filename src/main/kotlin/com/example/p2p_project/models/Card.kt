package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.Bank
import jakarta.persistence.*

@Entity
data class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "card_number")
    val cardNumber: String,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "bank_id")
    val bank: Bank
)