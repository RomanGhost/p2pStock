package com.example.p2p_stock.components.trigger

import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.repositories.WalletRepository
import jakarta.persistence.PostUpdate
import jakarta.transaction.Transaction
import org.hibernate.TransactionException
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class DealStatusListener(
    private val walletRepository: WalletRepository,
    private val transactionTemplate: TransactionTemplate
) {
    @PostUpdate
    fun handleDealStatusChange(deal: Deal) {
        if (deal.status!!.name == "Закрыто: успешно") {
            transactionTemplate.executeWithoutResult {
                transferMoney(deal)
            }
        }
    }

    private fun transferMoney(deal: Deal) {
        val seller = walletRepository.findById(deal.sellOrder?.wallet!!.id).orElseThrow()
        val buyer = walletRepository.findById(deal.buyOrder?.wallet!!.id).orElseThrow()

        if (seller.balance < deal.sellOrder!!.quantity) {
            throw TransactionException("Not enough balance for transfer")
        }

        seller.balance -= deal.sellOrder!!.quantity
        buyer.balance += deal.sellOrder!!.quantity

        walletRepository.saveAll(listOf(seller, buyer))
    }
}