package com.example.p2p_stock.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "deal_statuses")
data class DealStatus(
    @Id
    @Column(length = 32)
    val name: String = ""
)

@Entity
@Table(name = "deals")
data class Deal(
    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long=0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_order_id", nullable = false)
    var buyOrder: Order?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_order_id", nullable = false)
    var sellOrder: Order?=null,

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    var status: DealStatus?=null,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    //TODO(Убрать и так понятно когда будет закрыто)
    @Column(name = "closed_at")
    var closedAt: LocalDateTime? = null,

    @Column(name = "last_status_change")
    var lastStatusChange: LocalDateTime = LocalDateTime.now()
)
