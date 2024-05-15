package com.example.p2p_project.services

import com.example.p2p_project.models.Card
import com.example.p2p_project.repositories.CardRepository
import com.example.p2p_project.repositories.RequestRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service

@Service
class CardService(
    private val cardRepository: CardRepository,
    private val requestRepository: RequestRepository
) {
    fun getAll():List<Card>{
        return cardRepository.findAll()
    }

    fun getById(id:Long):Card{
        val card = try{
            cardRepository.getReferenceById(id)
        }catch (ex: JpaObjectRetrievalFailureException){
            throw EntityNotFoundException("Card with id: $id not found")
        }
        return card
    }

    fun update(card: Card, id:Long): Card {
        card.id = id
        if (cardRepository.existsById(id))
            return cardRepository.save(card)
        else
            throw EntityNotFoundException("Card with id: $id not found")
    }

    fun deleteById(cardId:Long){
        // Найдем все заявки, связанные с этой картой
        val requestsWithCard = requestRepository.findByCardId(cardId)

        // Уберем ссылку на карту у каждой найденной заявки
        requestsWithCard.forEach { it.card = null }

        // Сохраняем обновленные заявки
        requestRepository.saveAll(requestsWithCard)

        // Удаляем карту
        return cardRepository.deleteById(cardId)

    }

    fun add(card: Card): Card {
        return cardRepository.save(card)
    }

    fun getByUserId(userId:Long):List<Card>{
        return cardRepository.findByUserId(userId)
    }

    fun existsByCardNumber(cardNumber:String):Boolean{
        return cardRepository.existsByCardNumber(cardNumber)
    }
}