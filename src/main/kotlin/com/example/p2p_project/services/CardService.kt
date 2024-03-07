package com.example.p2p_project.services

import com.example.p2p_project.models.Card
import com.example.p2p_project.repositories.CardRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service

@Service
class CardService(val cardRepository: CardRepository) {
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

    fun delete(id:Long){
        return cardRepository.deleteById(id)
    }

    fun add(card: Card): Card {
        return cardRepository.save(card)
    }

    fun getByUserId(userId:Long):List<Card>{
        return cardRepository.findByUserId(userId)
    }
}