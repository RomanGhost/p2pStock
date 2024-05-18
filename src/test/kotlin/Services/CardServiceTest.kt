package com.example.p2p_project.services.Services

import com.example.p2p_project.models.Card
import com.example.p2p_project.repositories.CardRepository
import com.example.p2p_project.repositories.RequestRepository
import com.example.p2p_project.services.CardService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class CardServiceTest {

    private lateinit var cardRepository: CardRepository
    private lateinit var requestRepository: RequestRepository
    private lateinit var cardService: CardService

    @BeforeEach
    fun setup() {
        cardRepository = mock(CardRepository::class.java)
        requestRepository = mock(RequestRepository::class.java)
        cardService = CardService(cardRepository, requestRepository)
    }

    @Test
    fun `should return all cards`() {
        val cards = listOf(Card(), Card())
        `when`(cardRepository.findAll()).thenReturn(cards)

        val result = cardService.getAll()
        assertEquals(cards, result)
    }

    @Test
    fun `should return card by id`() {
        val card = Card()
        `when`(cardRepository.getReferenceById(1L)).thenReturn(card)

        val result = cardService.getById(1L)
        assertEquals(card, result)
    }

    @Test
    fun `should throw exception when card not found by id`() {
        `when`(cardRepository.getReferenceById(1L)).thenThrow(EntityNotFoundException::class.java)

        assertThrows(EntityNotFoundException::class.java) {
            cardService.getById(1L)
        }
    }

    @Test
    fun `should add new card`() {
        val card = Card()
        `when`(cardRepository.save(card)).thenReturn(card)

        val result = cardService.add(card)
        assertEquals(card, result)
    }

    @Test
    fun `should update card`() {
        val card = Card()
        `when`(cardRepository.existsById(1L)).thenReturn(true)
        `when`(cardRepository.save(card)).thenReturn(card)

        val result = cardService.update(card, 1L)
        assertEquals(card, result)
    }

    @Test
    fun `should throw exception when updating non-existing card`() {
        val card = Card()
        `when`(cardRepository.existsById(1L)).thenReturn(false)

        assertThrows(EntityNotFoundException::class.java) {
            cardService.update(card, 1L)
        }
    }

    @Test
    fun `should delete card by id`() {
        cardService.deleteById(1L)
        verify(cardRepository, times(1)).deleteById(1L)
    }

    @Test
    fun `should return cards by user id`() {
        val cards = listOf(Card(), Card())
        `when`(cardRepository.findByUserId(1L)).thenReturn(cards)

        val result = cardService.getByUserId(1L)
        assertEquals(cards, result)
    }

    @Test
    fun `should check if card number exists`() {
        `when`(cardRepository.existsByCardNumber("1234567812345678")).thenReturn(true)

        val result = cardService.existsByCardNumber("1234567812345678")
        assertTrue(result)
    }
}
