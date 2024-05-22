package com.example.p2p_project.services.adjacent

import com.example.p2p_project.models.adjacent.DealAction
import com.example.p2p_project.models.dataTables.Priority
import com.example.p2p_project.repositories.adjacent.DealActionRepository
import com.example.p2p_project.repositories.dataTables.PriorityRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DealActionServiceTest {

    private lateinit var dealActionRepository: DealActionRepository
    private lateinit var priorityRepository: PriorityRepository
    private lateinit var dealActionService: DealActionService

    @BeforeEach
    fun setup() {
        dealActionRepository = mock(DealActionRepository::class.java)
        priorityRepository = mock(PriorityRepository::class.java)
        dealActionService = DealActionService(dealActionRepository, priorityRepository)
    }

    @Test
    fun `should add deal action`() {
        val dealAction = DealAction()
        `when`(dealActionRepository.save(dealAction)).thenReturn(dealAction)

        val result = dealActionService.addDealAction(dealAction)
        assertEquals(dealAction, result)
    }

    @Test
    fun `should add deal action using add method`() {
        val dealAction = DealAction()
        `when`(dealActionRepository.save(dealAction)).thenReturn(dealAction)

        val result = dealActionService.add(dealAction)
        assertEquals(dealAction, result)
    }

    @Test
    fun `should set priority for deal action`() {
        val dealAction = DealAction()
        val priority = Priority()
        `when`(priorityRepository.findByType("High")).thenReturn(priority)
        `when`(dealActionRepository.save(dealAction)).thenReturn(dealAction)

        val result = dealActionService.setPriority(dealAction, "High")
        assertEquals(priority, result.priority)
    }

    @Test
    fun `should throw exception when priority not found`() {
        val dealAction = DealAction()
        `when`(priorityRepository.findByType("High")).thenReturn(null)

        val exception = assertThrows(EntityNotFoundException::class.java) {
            dealActionService.setPriority(dealAction, "High")
        }
        assertEquals("Priority with name High not found", exception.message)
    }
}
