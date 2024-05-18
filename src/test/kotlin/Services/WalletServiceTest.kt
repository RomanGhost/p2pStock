package com.example.p2p_project.services.Services

import com.example.p2p_project.models.Wallet
import com.example.p2p_project.repositories.RequestRepository
import com.example.p2p_project.repositories.WalletRepository
import com.example.p2p_project.services.WalletService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class WalletServiceTest {

    private lateinit var walletRepository: WalletRepository
    private lateinit var requestRepository: RequestRepository
    private lateinit var walletService: WalletService

    @BeforeEach
    fun setup() {
        walletRepository = mock(WalletRepository::class.java)
        requestRepository = mock(RequestRepository::class.java)
        walletService = WalletService(walletRepository, requestRepository)
    }

    @Test
    fun `should return all wallets`() {
        val wallets = listOf(Wallet(), Wallet())
        `when`(walletRepository.findAll()).thenReturn(wallets)

        val result = walletService.getAll()
        assertEquals(wallets, result)
    }

    @Test
    fun `should return wallet by id`() {
        val wallet = Wallet()
        `when`(walletRepository.getReferenceById(1L)).thenReturn(wallet)

        val result = walletService.getById(1L)
        assertEquals(wallet, result)
    }

    @Test
    fun `should throw exception when wallet not found by id`() {
        `when`(walletRepository.getReferenceById(1L)).thenThrow(EntityNotFoundException::class.java)

        assertThrows(EntityNotFoundException::class.java) {
            walletService.getById(1L)
        }
    }

    @Test
    fun `should add new wallet`() {
        val wallet = Wallet()
        `when`(walletRepository.save(wallet)).thenReturn(wallet)

        val result = walletService.add(wallet)
        assertEquals(wallet, result)
    }

    @Test
    fun `should update wallet`() {
        val wallet = Wallet()
        `when`(walletRepository.existsById(1L)).thenReturn(true)
        `when`(walletRepository.save(wallet)).thenReturn(wallet)

        val result = walletService.update(wallet, 1L)
        assertEquals(wallet, result)
    }

    @Test
    fun `should throw exception when updating non-existing wallet`() {
        val wallet = Wallet()
        `when`(walletRepository.existsById(1L)).thenReturn(false)

        assertThrows(EntityNotFoundException::class.java) {
            walletService.update(wallet, 1L)
        }
    }

    @Test
    fun `should delete wallet by id`() {
        walletService.deleteById(1L)
        verify(walletRepository, times(1)).deleteById(1L)
    }

    @Test
    fun `should return wallets by user id`() {
        val wallets = listOf(Wallet(), Wallet())
        `when`(walletRepository.findByUserId(1L)).thenReturn(wallets)

        val result = walletService.getByUserId(1L)
        assertEquals(wallets, result)
    }

    @Test
    fun `should transfer balance`() {
        val walletFrom = Wallet()
        walletFrom.balance = 100.0
        val walletTo = Wallet()
        walletTo.balance = 50.0

        `when`(walletRepository.getReferenceById(1L)).thenReturn(walletFrom)
        `when`(walletRepository.getReferenceById(2L)).thenReturn(walletTo)

        walletService.transferBalance(1L, 2L, 25.0)

        assertEquals(75.0, walletFrom.balance)
        assertEquals(75.0, walletTo.balance)
    }
}
