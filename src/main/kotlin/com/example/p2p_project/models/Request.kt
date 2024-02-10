package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.RequestStatus
import com.example.p2p_project.models.dataTables.RequestType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "wallet_id")
    val wallet: Wallet,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "card_id")
    val card: Card,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "request_type_id")
    val requestType: RequestType,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "request_status_id")
    val requestStatus: RequestStatus,

    @Column(name = "price_per_unit")
    val pricePerUnit: BigDecimal,
    val quantity: Float,
    val description: String,
    @Column(name = "date_time")
    val dateTime: LocalDateTime
)