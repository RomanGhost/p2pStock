package com.example.p2p_project.services.Services

import com.example.p2p_project.models.Request
import com.example.p2p_project.models.User
import com.example.p2p_project.repositories.RequestRepository
import com.example.p2p_project.repositories.dataTables.RequestStatusRepository
import com.example.p2p_project.services.RequestService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class RequestServiceTest {

    private lateinit var requestRepository: RequestRepository
    private lateinit var requestStatusRepository: RequestStatusRepository
    private lateinit var requestService: RequestService

    @BeforeEach
    fun setup() {
        requestRepository = mock(RequestRepository::class.java)
        requestStatusRepository = mock(RequestStatusRepository::class.java)
        requestService = RequestService(requestRepository, requestStatusRepository)
    }

    @Test
    fun `should add new request`() {
        val request = Request()
        `when`(requestStatusRepository.findByName("Модерация")).thenReturn(null)
        `when`(requestRepository.save(request)).thenReturn(request)

        val result = requestService.add(request, "Модерация")
        assertEquals(request, result)
    }

    @Test
    fun `should return all requests`() {
        val requests = listOf(Request(), Request())
        `when`(requestRepository.findAll()).thenReturn(requests)

        val result = requestService.getAll()
        assertEquals(requests, result)
    }

    @Test
    fun `should return request by id`() {
        val request = Request()
        `when`(requestRepository.getReferenceById(1L)).thenReturn(request)

        val result = requestService.getById(1L)
        assertEquals(request, result)
    }

    @Test
    fun `should throw exception when request not found by id`() {
        `when`(requestRepository.getReferenceById(1L)).thenThrow(EntityNotFoundException::class.java)

        assertThrows(EntityNotFoundException::class.java) {
            requestService.getById(1L)
        }
    }

    @Test
    fun `should update request`() {
        val request = Request()
        `when`(requestRepository.existsById(1L)).thenReturn(true)
        `when`(requestRepository.save(request)).thenReturn(request)

        val result = requestService.update(request)
        assertEquals(request, result)
    }

    @Test
    fun `should throw exception when updating non-existing request`() {
        val request = Request()
        `when`(requestRepository.existsById(1L)).thenReturn(false)

        assertThrows(EntityNotFoundException::class.java) {
            requestService.update(request)
        }
    }

    @Test
    fun `should delete request by id`() {
        requestService.delete(1L)
        verify(requestRepository, times(1)).deleteById(1L)
    }

    @Test
    fun `should return requests by user id`() {
        val requests = listOf(Request(), Request())
        `when`(requestRepository.findByUserId(1L)).thenReturn(requests)

        val result = requestService.getByUserId(1L)
        assertEquals(requests, result)
    }

    @Test
    fun `should update request status`() {
        val request = Request()
        `when`(requestRepository.existsById(1L)).thenReturn(true)
        `when`(requestRepository.getReferenceById(1L)).thenReturn(request)
        `when`(requestStatusRepository.findByName("Закрыто: успешно")).thenReturn(null)
        `when`(requestRepository.save(request)).thenReturn(request)

        val result = requestService.updateStatusById(1L, "Закрыто: успешно")
        assertEquals(request, result)
    }

    @Test
    fun `should add manager to request`() {
        val user = User()
        val request = Request()
        `when`(requestRepository.getReferenceById(1L)).thenReturn(request)
        `when`(requestRepository.save(request)).thenReturn(request)

        val result = requestService.addManager(user, 1L)
        assertEquals(user, result.managerId)
    }
}
