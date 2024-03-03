package com.example.p2p_project.models

import com.example.p2p_project.models.dataTables.RequestStatus
import com.example.p2p_project.models.dataTables.RequestType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?=0,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "wallet_id")
    val wallet: Wallet=Wallet(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    val user: User=User(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "card_id")
    val card: Card = Card(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "request_type_id")
    val requestType: RequestType= RequestType(),

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "request_status_id")
    val requestStatus: RequestStatus = RequestStatus(),

    @Column(name = "price_per_unit")
    val pricePerUnit: Double =0.0,
    val quantity: Double= 0.0,
    val description: String="",
    @Column(name = "create_date_time")
    val createDateTime: LocalDateTime?=null,
    @Column(name = "deadline_date_time")
    val deadlineDateTime:LocalDateTime? = null
)