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
    val id: Long=-1,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    val deal: Deal?=null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    var manager: User?=null,

    @Column(nullable = true)
    var confirmation: Boolean?=null,

    @Column(length = 1024)
    val errorDescription: String = "",

    @Column(length = 1024)
    var result: String = "",

    @ManyToOne
    @JoinColumn(name = "priority_id", nullable = false)
    var priority: Priority?=null,

    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = LocalDateTime.now()
)
