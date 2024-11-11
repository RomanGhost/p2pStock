package com.example.p2p_stock.initialize

import com.example.p2p_stock.models.Bank
import com.example.p2p_stock.repositories.BankRepository
import jakarta.transaction.Transactional
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BankInitialize(val bankRepository: BankRepository) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        val bankNames = listOf("Сбербанк", "Альфа-Банк", "ВТБ", "Газпромбанк", "Т-Банк")

        val existingBanks = bankRepository.findAll()

        for (bankName in bankNames) {
            val exists = existingBanks.any { it.name.equals(bankName, ignoreCase = true) }
            if (!exists) {
                // Если банк не существует, сохраняем его в репозитории
                val bank = Bank(bankName)
                bankRepository.save(bank)
            }
        }
    }
}