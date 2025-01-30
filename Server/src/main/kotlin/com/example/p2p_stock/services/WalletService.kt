package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.WalletInfo
import com.example.p2p_stock.errors.DuplicateWalletException
import com.example.p2p_stock.errors.NotFoundWalletException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.User
import com.example.p2p_stock.models.Wallet
import com.example.p2p_stock.repositories.WalletRepository
import com.example.p2p_stock.services.sender.ApiService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val cryptocurrencyService: CryptocurrencyService,
    private val senderApiService: ApiService
) {

    fun findById(walletId:Long): Wallet{
        return walletRepository.findById(walletId).orElseThrow {
            NotFoundWalletException("Wallet with Id:$walletId not found")
        }
    }

    fun findByPublicKey(publicKey:String): Wallet?{
        return walletRepository.findByPublicKey(publicKey)
    }

    fun findByUserId(userId: Long): List<Wallet> = walletRepository.findByUserId(userId)

    fun save(wallet: Wallet): Wallet = walletRepository.save(wallet)

    fun addWallet(walletInfo: WalletInfo, user: User): Wallet {
        val walletNames:List<String> = findByUserId(user.id).map { it.name }
        if (walletInfo.walletName in walletNames){
            throw DuplicateWalletException("Кошелек с таким названием уже существует")
        }

        val crypto = cryptocurrencyService.findByCode(walletInfo.cryptocurrencyCode)
        val key = senderApiService.generateKeys()

        val newWallet = Wallet(
            cryptocurrency = crypto,
            balance = 0.0,
            name=walletInfo.walletName,
            user = user,
            publicKey = key.publicKey,
            privateKey = key.privateKey,
        )

        try {
            return walletRepository.save(newWallet)
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateWalletException("Кошелек уже существует")
        }
    }

    fun validateOwnership(walletId: Long, user:User): Wallet {
        val wallet = walletRepository.findById(walletId).getOrNull()

        if (wallet?.user?.id != user.id) {
            throw OwnershipException("Wallet with id ${walletId} does not belong to user ${user.id}")
        }

        return wallet
    }
    
    fun delete(walletId: Long, userId: Long) {
        val wallet = walletRepository.findById(walletId)
        if (wallet.get().user!!.id == userId)
            walletRepository.deleteById(walletId)
    }
}