package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.Request
import com.example.p2p_project.models.dataTables.OperationType
import com.example.p2p_project.models.Wallet
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class WalletOperation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    val wallet: Wallet,

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = true)
    val request:Request,

    @ManyToOne
    @JoinColumn(name = "counterparty_id")
    val counterparty: Wallet,

    val count: Double,

    @Column(name = "date_time")
    val dateTime: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "operation_id")
    val operation: OperationType
)
