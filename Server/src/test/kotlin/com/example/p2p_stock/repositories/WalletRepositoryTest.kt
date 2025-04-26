package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Cryptocurrency
import com.example.p2p_stock.models.User
import com.example.p2p_stock.models.Wallet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.BeforeTest

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private lateinit var walletRepository: WalletRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var cryptocurrencyRepository: CryptocurrencyRepository
    private var userID:Long = 0

    @BeforeEach
    fun setup() {
        val user = userRepository.save(User(id = 1))
        val crypto = cryptocurrencyRepository.save(
            Cryptocurrency(name = "Bitcoin", code = "BTC")
        )
        userID = user.id

        val wallet = Wallet(user = user, publicKey = "abc123", cryptocurrency = crypto)
        walletRepository.save(wallet)
    }

    @Test
    fun `should find wallet by userId`() {
        val found = walletRepository.findByUserId(userID)

        println("Found by user: $found")

        assertEquals(1, found.size)
        assertEquals("abc123", found[0].publicKey)
    }

    @Test
    fun `should find wallet by publicKey`() {
        val found = walletRepository.findByPublicKey("abc123")

        assertNotNull(found)
        assertEquals(userID, found?.user?.id)
    }

    @Test
    fun `should return null for non-existent publicKey`() {
        val found = walletRepository.findByPublicKey("not_exist")
        assertNull(found)
    }
}
