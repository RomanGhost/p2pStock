package com.example.p2p_project.models

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class Deal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

//    @ManyToOne
//    @JoinColumn(name = "status_id")
    val status: Int, //

    @Column(name = "date_time")
    val dateTime: LocalDateTime,

//    @ManyToOne
//    @JoinColumn(name = "sell_request_id")
    val sellRequest:Int, // Request

//    @ManyToOne
//    @JoinColumn(name = "buy_request_id")
    val buyRequest:Int // Request
)
