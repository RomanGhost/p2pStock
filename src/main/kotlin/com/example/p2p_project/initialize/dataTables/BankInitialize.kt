package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.Bank
import com.example.p2p_project.repositories.dataTables.BankRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component

class BankInitialize(val bankRepository: BankRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (bankRepository.count() != 0L) return;
        val banks:MutableList<Bank> = mutableListOf()

        banks.add(Bank(-1,"Сбербанк"))
        banks.add(Bank(-1,"Альфа-Банк"))
        banks.add(Bank(-1,"ВТБ"))
        banks.add(Bank(-1,"Газпромбанк"))
        banks.add(Bank(-1,"Тинькофф"))
        banks.add(Bank(-1,"СБП"))

        for (bank in banks){
            bankRepository.save(bank)
        }
    }
}