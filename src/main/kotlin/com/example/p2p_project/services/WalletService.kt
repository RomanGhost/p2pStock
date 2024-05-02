package com.example.p2p_project.services

import com.example.p2p_project.models.Wallet
import com.example.p2p_project.repositories.RequestRepository
import com.example.p2p_project.repositories.WalletRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.time.LocalTime

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

    fun add(wallet: Wallet): Wallet {
        //TODO("Change to Addr")
        wallet.publicKey = LocalTime.now().toString()
        return walletRepository.save(wallet)
    }

    fun getByUserId(userId:Long):List<Wallet>{
        return walletRepository.findByUserId(userId)
    }

    fun getByCryptocurrencyId(cryptocurrencyId:Long):List<Wallet> {
        return walletRepository.findByCryptocurrencyId(cryptocurrencyId)
    }
}