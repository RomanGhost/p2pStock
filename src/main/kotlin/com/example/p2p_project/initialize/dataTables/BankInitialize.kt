package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.Bank
import com.example.p2p_project.repositories.dataTables.BankRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BankInitialize(val bankRepository: BankRepository): CommandLineRunner {
    override fun run(vararg args: String?) {
        if (bankRepository.count() != 0L) return;
        val banks:MutableList<Bank> = mutableListOf()

        banks.add(Bank(null,"Сбербанк"))
        banks.add(Bank(null,"Альфа-Банк"))
        banks.add(Bank(null,"ВТБ"))
        banks.add(Bank(null,"Газпромбанк"))
        banks.add(Bank(null,"Тинькофф"))
        banks.add(Bank(null,"СБП"))

        for (bank in banks){
            bankRepository.save(bank)
        }
    }
}