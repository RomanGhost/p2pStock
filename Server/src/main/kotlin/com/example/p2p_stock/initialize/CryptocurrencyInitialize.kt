package com.example.p2p_stock.initialize

import com.example.p2p_stock.models.Cryptocurrency
import com.example.p2p_stock.repositories.CryptocurrencyRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CryptocurrencyInitialize(val cryptocurrencyRepository: CryptocurrencyRepository) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val cryptocurrencies = listOf(
            Cryptocurrency(name = "Bitcoin", code = "BTC"),
            Cryptocurrency(name = "Ethereum", code = "ETH") // Исправлено название на Ethereum
        )

        // Получаем существующие криптовалюты
        val existingCryptocurrencies = cryptocurrencyRepository.findAll()

        for (cryptocurrency in cryptocurrencies) {
            // Проверяем, существует ли уже криптовалюта с таким кодом
            val exists = existingCryptocurrencies.any { it.code.equals(cryptocurrency.code, ignoreCase = true) }
            if (!exists) {
                cryptocurrencyRepository.save(cryptocurrency)
            }
        }
    }
}
