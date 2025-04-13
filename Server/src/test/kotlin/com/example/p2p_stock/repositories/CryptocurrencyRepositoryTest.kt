package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Cryptocurrency
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.*

@DataJpaTest
class CryptocurrencyRepositoryTest {

    @Autowired
    private lateinit var cryptocurrencyRepository: CryptocurrencyRepository

    @Test
    fun `should save and retrieve cryptocurrency by code`() {
        val crypto = Cryptocurrency(name = "Ethereum", code = "ETH")
        cryptocurrencyRepository.save(crypto)

        val found = cryptocurrencyRepository.findByCode("ETH")
        assertTrue(found.isPresent)
        assertEquals("Ethereum", found.get().name)
    }

    @Test
    fun `should return empty when cryptocurrency not found`() {
        val result = cryptocurrencyRepository.findByCode("DOGE")
        assertTrue(result.isEmpty)
    }
}
