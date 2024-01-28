package com.example.p2p_project.services

import com.example.p2p_project.models.Deal
import com.example.p2p_project.repositories.DealRepository
import org.springframework.stereotype.Service

@Service
class DealService(val dealRepository: DealRepository) {
    fun getAll(): List<Deal> {
        return dealRepository.findAll()
    }

    fun getById(id: Long): Deal {
        return dealRepository.getReferenceById(id)
    }

    fun update(deal:Deal, id:Long?): Deal {
        deal.id = id?:deal.id
        return dealRepository.save(deal)
    }

    fun delete(id:Long){
        dealRepository.deleteById(id)
    }

    fun add(deal:Deal): Deal {
        return dealRepository.save(deal)

    }
}