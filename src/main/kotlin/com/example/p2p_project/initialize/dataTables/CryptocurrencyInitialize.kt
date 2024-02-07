package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.Cryptocurrency
import com.example.p2p_project.repositories.dataTables.CryptocurrencyRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class CryptocurrencyInitialize(val cryptocurrencyRepository: CryptocurrencyRepository): CommandLineRunner {
    override fun run(vararg args: String?) {
        if (cryptocurrencyRepository.count() != 0L) return;
        val cryptocurrencies: MutableList<Cryptocurrency> = mutableListOf()

        cryptocurrencies.add(Cryptocurrency(null, "Bitcoin", "BTC"))
        cryptocurrencies.add(Cryptocurrency(null, "Etherium", "ETH"))

        for (cryptocurrency in cryptocurrencies) {
            cryptocurrencyRepository.save(cryptocurrency)
        }
    }
}