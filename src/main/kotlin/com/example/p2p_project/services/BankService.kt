package com.example.p2p_project.services

import com.example.p2p_project.models.dataTables.Bank
import com.example.p2p_project.repositories.dataTables.BankRepository
import org.springframework.stereotype.Service

@Service
class BankService(val bankRepository: BankRepository) {
    fun getAll(): List<Bank> {
        return bankRepository.findAll()
    }

    fun getByName(bankName:String): List<Bank>{
        return bankRepository.findByName(bankName)
    }
}