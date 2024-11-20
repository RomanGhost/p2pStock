package com.example.p2p_stock.models

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "order_types")
data class OrderType(
    @Id
    @Column(length = 16)
    val name: String = ""
)

@Entity
@Table(name = "order_statuses")
data class OrderStatus(
    @Id
    @Column(length = 32)
    val name: String = ""
)

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long=0,

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    var wallet: Wallet?=null,

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    var card: Card?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User?=null,

    @ManyToOne
    @JoinColumn(name = "order_type_id", nullable = false)
    var type: OrderType?=null,

    @ManyToOne
    @JoinColumn(name = "order_status_id", nullable = false)
    var status: OrderStatus?=null,

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal=BigDecimal(0),

    @Column(nullable = false)
    var quantity: Double=0.0,

    @Column(length = 1024)
    var description: String = "",

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expiry_at")
    val expiryAt: LocalDateTime? = LocalDateTime.now(),

    @Column(name = "last_status_change")
    var lastStatusChange: LocalDateTime? = null
)
