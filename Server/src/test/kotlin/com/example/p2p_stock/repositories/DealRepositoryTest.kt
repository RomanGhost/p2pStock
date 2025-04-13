package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal
import java.time.LocalDateTime

@DataJpaTest
class DealRepositoryTest {

    @Autowired private lateinit var dealRepository: DealRepository
    @Autowired private lateinit var orderRepository: OrderRepository
    @Autowired private lateinit var orderTypeRepository: OrderTypeRepository
    @Autowired private lateinit var orderStatusRepository: OrderStatusRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var walletRepository: WalletRepository
    @Autowired private lateinit var cryptocurrencyRepository: CryptocurrencyRepository
    @Autowired private lateinit var dealStatusRepository: DealStatusRepository

    private lateinit var buyOrder: Order
    private lateinit var sellOrder: Order

    @BeforeEach
    fun setup() {
        val role = roleRepository.save(Role("USER"))
        val user1 = userRepository.save(User(0, "buyer", "pass", "buyer@test.com", true, role=role))
        val user2 = userRepository.save(User(0, "seller", "pass", "seller@test.com", true, role=role))

        val crypto = cryptocurrencyRepository.save(Cryptocurrency("Bitcoin", "BTC"))

        val wallet1 = walletRepository.save(Wallet(user = user1, cryptocurrency = crypto, balance = 1000.0, publicKey = "123", privateKey = "123"))
        val wallet2 = walletRepository.save(Wallet(user = user2, cryptocurrency = crypto, balance = 1000.0, publicKey = "456", privateKey = "456"))

        val orderTypeBuy = orderTypeRepository.save(OrderType("BUY"))
        val orderTypeSell = orderTypeRepository.save(OrderType("SELL"))
        val orderStatus = orderStatusRepository.save(OrderStatus("OPEN"))

        buyOrder = orderRepository.save(
            Order(
                id = 0,
                wallet = wallet1,
                type = orderTypeBuy,
                status = orderStatus,
                unitPrice = BigDecimal("50000.00"),
                quantity = 0.5,
                description = "Buy BTC"
            )
        )

        sellOrder = orderRepository.save(
            Order(
                id = 1,
                wallet = wallet2,
                type = orderTypeSell,
                status = orderStatus,
                unitPrice = BigDecimal("50000.00"),
                quantity = 0.5,
                description = "Sell BTC"
            )
        )
        val dealStatus = dealStatusRepository.save(DealStatus("INIT"))
        val deal = Deal(id=1, buyOrder = buyOrder, sellOrder = sellOrder, status = dealStatus )
        dealRepository.save(deal)
    }

    @Test
    fun `should find deal by buyOrderId`() {
        val deal = dealRepository.findByBuyOrderId(buyOrder.id)
        assertTrue(deal.isPresent)
        assertEquals(buyOrder.id, deal.get().buyOrder?.id)
    }

    @Test
    fun `should find deal by sellOrderId`() {
        val deal = dealRepository.findBySellOrderId(sellOrder.id)
        assertTrue(deal.isPresent)
        assertEquals(sellOrder.id, deal.get().sellOrder?.id)
    }

    @Test
    fun `should find latest deal ID`() {
        val latestId = dealRepository.findLatestDeal()
        assertNotNull(latestId)
        assertEquals(1L, latestId) // Assuming only one deal is saved
    }

    @Test
    fun `should return null when no deals exist`() {
        dealRepository.deleteAll()
        val latest = dealRepository.findLatestDeal()
        assertNull(latest)
    }
}
