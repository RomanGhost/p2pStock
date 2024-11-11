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
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    val wallet: Wallet,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "order_type_id", nullable = false)
    val type: OrderType,

    @ManyToOne
    @JoinColumn(name = "order_status_id", nullable = false)
    val status: OrderStatus,

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,

    @Column(nullable = false)
    val quantity: Double,

    @Column(length = 1024)
    val description: String = "",

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expiry_at")
    val expiryAt: LocalDateTime? = null,

    @Column(name = "last_status_change")
    var lastStatusChange: LocalDateTime? = null
)
