package com.example.p2p_stock.services

import com.example.p2p_stock.models.Cryptocurrency
import com.example.p2p_stock.repositories.CryptocurrencyRepository
import org.springframework.stereotype.Service

@Service
class CryptocurrencyService(private val cryptocurrencyRepository: CryptocurrencyRepository) {

    fun findAll(): List<Cryptocurrency> = cryptocurrencyRepository.findAll()

    fun findByCode(code: String): Cryptocurrency? = cryptocurrencyRepository.findByCode(code).orElse(null)

    fun save(cryptocurrency: Cryptocurrency): Cryptocurrency = cryptocurrencyRepository.save(cryptocurrency)
}