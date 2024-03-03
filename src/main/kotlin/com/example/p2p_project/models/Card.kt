package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.Bank
import jakarta.persistence.*

@Entity
data class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    var user: User = User(),

    @Column(name = "card_number")
    var cardNumber: String = "",

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "bank_id")
    var bank: Bank = Bank()
)