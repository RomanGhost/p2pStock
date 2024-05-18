package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.RequestStatus
import com.example.p2p_project.models.dataTables.RequestType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long=0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_type_id")
    var requestType: RequestType = RequestType(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "wallet_id", nullable = true)
    var wallet: Wallet? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User=User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = true)
    var card: Card? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_status_id")
    var requestStatus: RequestStatus = RequestStatus(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    var managerId: User? = null,

    @Column(name = "price_per_unit")
    var pricePerUnit: Double = 0.0,

    @Column
    var quantity: Double = 0.0,

    @Column
    var description: String = "",

    @Column(name = "create_date_time")
    var createDateTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deadline_date_time")
    var deadlineDateTime:LocalDateTime = createDateTime,

    @Column(name = "last_change_date_time")
    var lastChangeStatusDateTime:LocalDateTime = createDateTime
)