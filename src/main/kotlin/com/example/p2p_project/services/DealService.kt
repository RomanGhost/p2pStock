package com.example.p2p_project.services

import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.dataTables.DealStatus
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

    fun updateStatus(id:Long, status:String):Deal{
        if (!dealRepository.existsById(id))
            throw EntityNotFoundException("Deal with id: $id not found")
        val dealStatus = DealStatus(name=status)
        val deal = dealRepository.getReferenceById(id)
        deal.status = dealStatus
        dealRepository.save(deal)

        return deal
    }

    fun delete(id:Long){
        dealRepository.deleteById(id)
    }

    fun add(deal:Deal): Deal {
        return dealRepository.save(deal)
    }

    fun checkDealAccess(dealId: Long, userId: Long): Boolean {
        val deal = this.getById(dealId)
        return deal.buyRequest.user.id == userId || deal.sellRequest.user.id == userId
    }

    fun initiatorAccept(dealId: Long, userId: Long, status:String): Boolean {
        val deal = this.getById(dealId)
        return ((deal.isBuyCreated == true && deal.sellRequest.user.id == userId) ||
                (deal.isBuyCreated == false && deal.buyRequest.user.id == userId)) &&
                deal.status.name == status

    }

    fun counterpartyAccept(dealId: Long, userId: Long, status: String):Boolean{
        val deal = this.getById(dealId)
        return ((deal.isBuyCreated == true && deal.buyRequest.user.id == userId) ||
                (deal.isBuyCreated == false && deal.sellRequest.user.id == userId)) &&
                deal.status.name == status
    }
}