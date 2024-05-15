package com.example.p2p_project.services

import com.example.p2p_project.models.Wallet
import com.example.p2p_project.repositories.RequestRepository
import com.example.p2p_project.repositories.WalletRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.time.LocalTime
import kotlin.math.abs

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val requestRepository: RequestRepository
) {
    fun getAll(): List<Wallet> {
        return walletRepository.findAll()
    }

    fun existWalletForUserId(nameWallet:String, userId: Long):Boolean{
        return walletRepository.existsByNameAndUserId(nameWallet, userId)
    }
    fun getById(id: Long): Wallet {
        val wallet = try{
            walletRepository.getReferenceById(id)
        }catch (ex: JpaObjectRetrievalFailureException){
            throw EntityNotFoundException("Wallet with id: $id not found")
        }
        return wallet
    }

    fun update(wallet: Wallet, id:Long): Wallet {
        wallet.id = id
        if (walletRepository.existsById(id))
            return walletRepository.save(wallet)
        else
            throw EntityNotFoundException("Wallet with id: $id not found")
    }

    fun deleteById(walletId:Long){
        val requestsWithWallet = requestRepository.findByWalletId(walletId)
        requestsWithWallet.forEach { it.wallet = null }

        // Сохраняем обновленные заявки
        requestRepository.saveAll(requestsWithWallet)

        walletRepository.deleteById(walletId)
    }

    fun transferBalance(walletIdFrom: Long, walletIdTo: Long, amount: Double) {
        val walletFrom = walletRepository.getReferenceById(walletIdFrom)
        val walletTo = walletRepository.getReferenceById(walletIdTo)

        walletFrom.balance -= amount
        walletTo.balance += amount

        walletRepository.save(walletFrom)
        walletRepository.save(walletTo)
    }

    fun add(wallet: Wallet): Wallet {
        //TODO("Change to Addr")
        wallet.publicKey = abs(LocalTime.now().hashCode()).toString()
        return walletRepository.save(wallet)
    }

    fun getByUserId(userId:Long):List<Wallet>{
        return walletRepository.findByUserId(userId)
    }

    fun getByCryptocurrencyId(cryptocurrencyId:Long):List<Wallet> {
        return walletRepository.findByCryptocurrencyId(cryptocurrencyId)
    }
}