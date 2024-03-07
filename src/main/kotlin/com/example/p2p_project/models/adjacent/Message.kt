package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Message (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1")
    val user1: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2")
    val user2: User = User(),

    @Column
    val message: String = "",

    @Column(name = "date_time")
    val createDateTime: LocalDateTime = LocalDateTime.now(),
    )
