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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long=0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_order_id", nullable = false)
    val buyOrder: Order?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_order_id", nullable = false)
    val sellOrder: Order?=null,

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    val status: DealStatus?=null,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "closed_at")
    var closedAt: LocalDateTime? = null,

    @Column(name = "last_status_change")
    var lastStatusChange: LocalDateTime = LocalDateTime.now()
)
