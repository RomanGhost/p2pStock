package com.example.p2p_stock.models

import jakarta.persistence.*

@Entity
@Table(name = "roles")
data class Role(
    @Id
    val name: String = ""
)



