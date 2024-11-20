package com.example.p2p_stock.services

import com.example.p2p_stock.models.Bank
import com.example.p2p_stock.repositories.BankRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class BankService(private val bankRepository: BankRepository) {

    fun findAll(): List<Bank> = bankRepository.findAll()

    fun findById(bankName:String): Optional<Bank> = bankRepository.findById(bankName)

    fun save(bank: Bank): Bank = bankRepository.save(bank)
}