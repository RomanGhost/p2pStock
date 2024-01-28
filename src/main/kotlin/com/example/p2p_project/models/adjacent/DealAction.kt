package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.dataTables.Action
import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.dataTables.ErrorModel
import jakarta.persistence.*

@Entity
data class DealAction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @ManyToOne
    @JoinColumn(name = "deal_id")
    val deal: Deal,

    @ManyToOne
    @JoinColumn(name = "action_id")
    val action: Action,

    val confirmation: Boolean,
    @Column(name = "action_description")
    val actionDescription: String,

    @ManyToOne
    @JoinColumn(name = "error_id")
    val errorModel: ErrorModel
)