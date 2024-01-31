package com.example.p2p_project.services

import com.example.p2p_project.models.Deal
import com.example.p2p_project.repositories.DealRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service

@Service
class DealService(val dealRepository: DealRepository) {
    fun getAll(): List<Deal> {
        return dealRepository.findAll()
    }

    fun getById(id: Long): Deal {
        val deal = try{
            dealRepository.getReferenceById(id)
        }catch (ex: JpaObjectRetrievalFailureException){
            throw EntityNotFoundException("Deal with id: $id not found")
        }
        return deal
    }

    fun update(deal:Deal, id:Long): Deal {
        deal.id = id
        if (dealRepository.existsById(id))
            return dealRepository.save(deal)
        else
            throw EntityNotFoundException("Deal with id: $id not found")
    }

    fun delete(id:Long){
        dealRepository.deleteById(id)
    }

    fun add(deal:Deal): Deal {
        return dealRepository.save(deal)
    }
}