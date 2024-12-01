package com.example.p2p_stock.dataclasses

import com.example.p2p_stock.models.Priority
import java.time.LocalDateTime

data class TaskInfo(
    val id: Long,
    val dealInfo: DealInfo,
    val managerLogin:String,
    val confirmation: Boolean?,
    val errorDescription: String,
    val result: String,
    var priorityName: String,
    var updatedAt: String
)

data class TaskPatchResultInfo(
    val result: String,
)
