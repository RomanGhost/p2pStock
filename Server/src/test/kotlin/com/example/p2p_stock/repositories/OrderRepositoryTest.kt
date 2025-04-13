package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal

@DataJpaTest
class OrderRepositoryTest {

    @Autowired private lateinit var orderRepository: OrderRepository
    @Autowired private lateinit var walletRepository: WalletRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var cryptocurrencyRepository: CryptocurrencyRepository
    @Autowired private lateinit var orderTypeRepository: OrderTypeRepository
    @Autowired private lateinit var orderStatusRepository: OrderStatusRepository

    private lateinit var wallet: Wallet

    @BeforeEach
    fun setup() {
        val role = roleRepository.save(Role("USER"))
        val user = userRepository.save(User(0, "user", "pass", "user@example.com", true, role=role))
        val crypto = cryptocurrencyRepository.save(Cryptocurrency("Bitcoin", "BTC"))
        wallet = walletRepository.save(Wallet(user = user, cryptocurrency = crypto, balance = 1000.0))

        val orderType = orderTypeRepository.save(OrderType("BUY"))
        val orderStatus = orderStatusRepository.save(OrderStatus("OPEN"))

        // Создание двух ордеров
        orderRepository.save(
            Order(
                id = 1,
                wallet = wallet,
                type = orderType,
                status = orderStatus,
                unitPrice = BigDecimal("45000.00"),
                quantity = 0.3,
                description = "First order"
            )
        )

        orderRepository.save(
            Order(
                id = 2,
                wallet = wallet,
                type = orderType,
                status = orderStatus,
                unitPrice = BigDecimal("46000.00"),
                quantity = 0.5,
                description = "Second order"
            )
        )
    }

    @Test
    fun `should find orders by wallet id`() {
        val orders = orderRepository.findByWalletId(wallet.id)
        assertEquals(2, orders.size)
        assertTrue(orders.all { it.wallet?.id == wallet.id })
    }

    @Test
    fun `should find latest order id`() {
        val latestId = orderRepository.findLatestOrder()
        assertNotNull(latestId)
        val maxId = orderRepository.findAll().maxOfOrNull { it.id }
        assertEquals(maxId, latestId)
    }

    @Test
    fun `should return null when no orders exist`() {
        orderRepository.deleteAll()
        val latest = orderRepository.findLatestOrder()
        assertNull(latest)
    }
}
