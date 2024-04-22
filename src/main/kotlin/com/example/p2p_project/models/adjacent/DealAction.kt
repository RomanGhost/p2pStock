package com.example.p2p_project.models.adjacent

import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.User
import com.example.p2p_project.models.dataTables.Priority
import jakarta.persistence.*

@Entity
data class DealAction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long=-1,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "deal_id")
    var deal: Deal = Deal(),

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "manager_id")
    var user: User = User(),

    @Column
    val confirmation: Boolean=false,
    @Column(name = "action_description")
    val actionDescription: String = "",
    @Column(name = "error_description")
    val errorModel: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="priority_id")
    var priority: Priority = Priority()
)