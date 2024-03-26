package com.example.p2p_project.services.dataServices

import com.example.p2p_project.models.dataTables.Cryptocurrency
import com.example.p2p_project.repositories.dataTables.CryptocurrencyRepository
import org.springframework.stereotype.Service

@Service
class CryptocurrencyService(private val cryptocurrencyRepository: CryptocurrencyRepository) {
    fun getAll(): List<Cryptocurrency> {
        return cryptocurrencyRepository.findAll()
    }
}