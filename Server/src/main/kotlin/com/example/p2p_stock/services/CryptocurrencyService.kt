package com.example.p2p_stock.services

import com.example.p2p_stock.models.Cryptocurrency
import com.example.p2p_stock.repositories.CryptocurrencyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CryptocurrencyService(private val cryptocurrencyRepository: CryptocurrencyRepository) {

    fun findAll(): List<Cryptocurrency> = cryptocurrencyRepository.findAll()

    fun findByCode(code: String): Cryptocurrency? = cryptocurrencyRepository.findByCode(code).orElse(null)

    @Transactional
    fun save(cryptocurrency: Cryptocurrency): Cryptocurrency = cryptocurrencyRepository.save(cryptocurrency)
}