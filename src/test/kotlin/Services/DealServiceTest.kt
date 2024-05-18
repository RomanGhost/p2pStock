package com.example.p2p_project.services.Services

import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.User
import com.example.p2p_project.repositories.DealRepository
import com.example.p2p_project.repositories.dataTables.DealStatusRepository
import com.example.p2p_project.services.DealService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class DealServiceTest {

    private lateinit var dealRepository: DealRepository
    private lateinit var dealStatusRepository: DealStatusRepository
    private lateinit var dealService: DealService

    @BeforeEach
    fun setup() {
        dealRepository = mock(DealRepository::class.java)
        dealStatusRepository = mock(DealStatusRepository::class.java)
        dealService = DealService(dealRepository, dealStatusRepository)
    }

    @Test
    fun `should return all deals`() {
        val deals = listOf(Deal(), Deal())
        `when`(dealRepository.findAll()).thenReturn(deals)

        val result = dealService.getAll()
        assertEquals(deals, result)
    }

    @Test
    fun `should return deal by id`() {
        val deal = Deal()
        `when`(dealRepository.getReferenceById(1L)).thenReturn(deal)

        val result = dealService.getById(1L)
        assertEquals(deal, result)
    }

    @Test
    fun `should throw exception when deal not found by id`() {
        `when`(dealRepository.getReferenceById(1L)).thenThrow(EntityNotFoundException::class.java)

        assertThrows(EntityNotFoundException::class.java) {
            dealService.getById(1L)
        }
    }

    @Test
    fun `should add new deal`() {
        val deal = Deal()
        `when`(dealRepository.save(deal)).thenReturn(deal)

        val result = dealService.add(deal)
        assertEquals(deal, result)
    }

    @Test
    fun `should update deal`() {
        val deal = Deal()
        `when`(dealRepository.existsById(1L)).thenReturn(true)
        `when`(dealRepository.save(deal)).thenReturn(deal)

        val result = dealService.update(deal, 1L)
        assertEquals(deal, result)
    }

    @Test
    fun `should throw exception when updating non-existing deal`() {
        val deal = Deal()
        `when`(dealRepository.existsById(1L)).thenReturn(false)

        assertThrows(EntityNotFoundException::class.java) {
            dealService.update(deal, 1L)
        }
    }

    @Test
    fun `should delete deal by id`() {
        dealService.delete(1L)
        verify(dealRepository, times(1)).deleteById(1L)
    }

    @Test
    fun `should return deals by user id`() {
        val deals = listOf(Deal(), Deal())
        `when`(dealRepository.findDealByUserId(1L)).thenReturn(deals)

        val result = dealService.getByUserId(1L)
        assertEquals(deals, result)
    }

    @Test
    fun `should check deal access`() {
        val user = User()
        user.id = 1L
        val deal = Deal()
        deal.buyRequest.user = user
        deal.sellRequest.user = user
        `when`(dealRepository.getReferenceById(1L)).thenReturn(deal)

        val result = dealService.checkDealAccess(1L, 1L)
        assertTrue(result)
    }
}
