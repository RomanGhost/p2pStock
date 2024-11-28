package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.CardInfo
import com.example.p2p_stock.errors.DuplicateCardException
import com.example.p2p_stock.errors.NotFoundCardException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.Card
import com.example.p2p_stock.models.User
import com.example.p2p_stock.repositories.CardRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class CardService(
    private val cardRepository: CardRepository,
    private val bankService: BankService,
) {

    fun findByUserId(userId: Long): List<Card> = cardRepository.findByUserId(userId)
    fun findById(cardId: Long): Card {
        return cardRepository.findById(cardId).orElseThrow {
            NotFoundCardException("Card with Id:$cardId not found for User")
        }
    }

    fun save(card: Card): Card = cardRepository.save(card)

    fun addCard(cardInfo: CardInfo, user: User): Card {
        val cardNames:List<String> = findByUserId(user.id).map { it.cardName }
        if (cardInfo.cardName in cardNames){
            throw DuplicateCardException("Карта с таким названием уже существует")
        }

        val bank = bankService.findById(cardInfo.bankName)
        val newCard = Card(
            cardNumber = cardInfo.cardNumber,
            cardName = cardInfo.cardName,
            bank = bank.get(),
            user = user
        )

        try {
            return cardRepository.save(newCard)
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateCardException("Карта уже существует")
        }
    }


    fun validateOwnership(cardId: Long, user:User): Card {
        val card = cardRepository.findById(cardId).getOrNull()

        if (card?.user?.id != user.id) {
            throw OwnershipException("Wallet with id $cardId does not belong to user ${user.id}")
        }

        return card
    }


    fun delete(cardId: Long, userId: Long) {
        val card = cardRepository.findById(cardId)
        if (card.get().user!!.id == userId)
            cardRepository.deleteById(cardId)
    }
}