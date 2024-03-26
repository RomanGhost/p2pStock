package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.DealStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Deal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=-1,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "status_id")
    val status: DealStatus = DealStatus(),

    @Column(name = "open_date_time")
    val openDateTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "close_date_time")
    val closeDateTime: LocalDateTime = LocalDateTime.now(),

    val isBuyCreated: Boolean?=null,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "sell_request_id")
    val sellRequest: Request = Request(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "buy_request_id")
    val buyRequest:Request = Request()
)
