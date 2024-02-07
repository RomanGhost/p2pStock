package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.dataTables.Action
import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.User
import com.example.p2p_project.models.dataTables.Priority
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
    @JoinColumn(name = "user_id")
    val user: User,

    val confirmation: Boolean,
    @Column(name = "action_description")
    val actionDescription: String,
    @Column(name = "error_description")
    val errorModel: String,

    @ManyToOne
    @JoinColumn(name="priority_id")
    val priority: Priority
)