package com.example.p2p_project.services

import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.User
import com.example.p2p_project.repositories.DealRepository
import com.example.p2p_project.repositories.dataTables.DealStatusRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DealService(val dealRepository: DealRepository, val dealStatusRepository: DealStatusRepository) {
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

    fun updateStatus(dealId:Long, status:String):Deal{
        if (!dealRepository.existsById(dealId))
            throw EntityNotFoundException("Deal with id: $dealId not found")
        val dealStatus = dealStatusRepository.findByName(status)
        val deal = dealRepository.getReferenceById(dealId)
        deal.status = dealStatus

        deal.changeLastTime = LocalDateTime.now()

        return dealRepository.save(deal)
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

    private fun initiatorAccept(deal: Deal, user: User, status:String): Boolean {
        return ((deal.isBuyCreated == true && deal.sellRequest.user.id == user.id) ||
                (deal.isBuyCreated == false && deal.buyRequest.user.id == user.id)) &&
                deal.status.name == status

    }

    private fun counterpartyAccept(deal: Deal, user: User, status: String):Boolean{
        return ((deal.isBuyCreated == true && deal.buyRequest.user.id == user.id) ||
                (deal.isBuyCreated == false && deal.sellRequest.user.id == user.id)) &&
                deal.status.name == status
    }

    fun acceptDeal(deal: Deal, user: User):Boolean{
        return initiatorAccept(deal, user, "Подтверждение сделки")
    }

    fun refuseDeal(deal:Deal):Boolean{
        return deal.status.name == "Подтверждение сделки" ||
                deal.status.name == "Ожидание перевода"
    }

    fun confirmPayment(deal:Deal, user: User):Boolean{
        // Если исходная заявка на покупку - то инициатор должен подтвердить перевод средств
        // Если исходная заявка на продажу - то контрагент должен подтвердить перевод средств
        return (initiatorAccept(deal, user, "Ожидание перевода")
                && deal.isBuyCreated==false)
        || (counterpartyAccept(deal, user, "Ожидание перевода")
                        && deal.isBuyCreated==true)
    }

    fun confirmReceipt(deal:Deal, user: User):Boolean {
        // Если исходная заявка на покупку - то аконтрагент должен подтвердить получение средств
        // Если исходная заявка на продажу - то инициатор должен подтвердить получение средств
        return ((initiatorAccept(deal, user, "Ожидание подтверждения перевода") ||
                initiatorAccept(deal, user, "Приостановлено: решение проблем"))
                && deal.isBuyCreated==true)
            || ((counterpartyAccept(deal, user, "Ожидание подтверждения перевода") ||
                counterpartyAccept(deal, user, "Приостановлено: решение проблем"))
                && deal.isBuyCreated==false)
    }

    fun denyReceipt(deal:Deal, user:User):Boolean{
        return (initiatorAccept(deal, user, "Ожидание подтверждения перевода")
                && deal.isBuyCreated==true)
                || (counterpartyAccept(deal, user, "Ожидание подтверждения перевода")
                && deal.isBuyCreated==false)
    }

    fun changeLastStatusTime(deal:Deal):Deal{
        deal.changeLastTime = LocalDateTime.now()
        val updateDeal = update(deal, deal.id)
        return updateDeal
    }
}