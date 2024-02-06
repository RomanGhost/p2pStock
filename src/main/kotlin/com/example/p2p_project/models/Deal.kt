package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.DealStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Deal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne
    @JoinColumn(name = "status_id")
    val status: DealStatus,

    @Column(name = "date_time")
    val dateTime: LocalDateTime,
    val isBuyCreated: Boolean,

    @ManyToOne
    @JoinColumn(name = "sell_request_id")
    val sellRequest: Request,

    @ManyToOne
    @JoinColumn(name = "buy_request_id")
    val buyRequest:Request
)
