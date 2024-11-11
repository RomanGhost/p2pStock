package com.example.p2p_stock.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "priorities")
data class Priority(
    @Id
    @Column(length = 32)
    val name: String = "",

    @Column(nullable = false)
    val level: Int = -1
)

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    val deal: Deal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    val manager: User,

    @Column(nullable = false)
    val confirmation: Boolean,

    @Column(length = 1024)
    val errorDescription: String = "",

    @ManyToOne
    @JoinColumn(name = "priority_id", nullable = false)
    val priority: Priority,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
