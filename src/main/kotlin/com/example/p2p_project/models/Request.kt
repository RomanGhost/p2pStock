package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.RequestStatus
import com.example.p2p_project.models.dataTables.RequestType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id")
    val requestType: RequestType = RequestType(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "wallet_id", nullable = true)
    val wallet: Wallet? = Wallet(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    var user: User=User(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "card_id", nullable = true)
    val card: Card? = Card(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_status_id")
    var requestStatus: RequestStatus = RequestStatus(),

    @Column(name = "price_per_unit")
    val pricePerUnit: Double = 0.0,

    @Column
    val quantity: Double= 0.0,

    @Column
    val description: String="",

    @Column(name = "create_date_time")
    var createDateTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deadline_date_time")
    var deadlineDateTime:LocalDateTime = createDateTime,

    @Column(name = "last_change_date_time")
    val lastChangeStatusDateTime:LocalDateTime = createDateTime
)