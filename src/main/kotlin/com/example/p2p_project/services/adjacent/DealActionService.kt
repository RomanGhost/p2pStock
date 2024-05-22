package com.example.p2p_project.services.adjacent

import com.example.p2p_project.models.adjacent.DealAction
import com.example.p2p_project.repositories.adjacent.DealActionRepository
import com.example.p2p_project.repositories.dataTables.PriorityRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class DealActionService(
    private val dealActionRepository: DealActionRepository,
    private val priorityRepository: PriorityRepository
){
    fun addDealAction(dealAction: DealAction): DealAction {
        return dealActionRepository.save(dealAction)
    }

    fun add(dealAction:DealAction): DealAction {
        return dealActionRepository.save(dealAction)
    }

    fun setPriority(dealAction: DealAction, priorityName: String): DealAction {
        val priority = priorityRepository.findByType(priorityName)
        dealAction.priority = priority
        return dealActionRepository.save(dealAction)
    }
}
