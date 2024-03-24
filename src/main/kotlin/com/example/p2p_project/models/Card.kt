package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.Bank
import jakarta.persistence.*

@Entity
data class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    var user: User = User(),

    @Column
    val cardName:String = "",

    @Column(name = "card_number", unique = true)
    var cardNumber: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    var bank: Bank = Bank()
)