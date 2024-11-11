package com.example.p2p_stock.services

import com.example.p2p_stock.models.Card
import com.example.p2p_stock.repositories.CardRepository
import org.springframework.stereotype.Service

@Service
class CardService(private val cardRepository: CardRepository) {

    fun findByUserId(userId: Long): List<Card> = cardRepository.findByUserId(userId)

    fun save(card: Card): Card = cardRepository.save(card)

    fun delete(id: Long) = cardRepository.deleteById(id)
}