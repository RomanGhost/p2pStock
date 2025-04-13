package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime

@DataJpaTest
class TaskRepositoryTest {

    @Autowired private lateinit var taskRepository: TaskRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var dealRepository: DealRepository
    @Autowired private lateinit var orderRepository: OrderRepository
    @Autowired private lateinit var walletRepository: WalletRepository
    @Autowired private lateinit var cryptocurrencyRepository: CryptocurrencyRepository
    @Autowired private lateinit var priorityRepository: PriorityRepository
    @Autowired private lateinit var typeRepository: OrderTypeRepository
    @Autowired private lateinit var statusRepository: OrderStatusRepository
    @Autowired private lateinit var dealStatusRepository: DealStatusRepository

    private lateinit var manager: User
    private lateinit var deal: Deal
    private lateinit var priority: Priority

    @BeforeEach
    fun setup() {
        // Role and User
        val role = roleRepository.save(Role("MANAGER"))
        manager = userRepository.save(User(
            login = "manager1",
            email = "manager@test.com",
            password = "pass",
            isActive = true,
            role = role
        ))

        // Crypto and Wallets
        val crypto = cryptocurrencyRepository.save(Cryptocurrency("Bitcoin", "BTC"))
        val buyer = userRepository.save(User(0, "buyer", "pass", "buyer@test.com", true, role=role))
        val seller = userRepository.save(User(0, "seller", "pass", "seller@test.com", true, role=role))
        val buyerWallet = walletRepository.save(Wallet(user = buyer, cryptocurrency = crypto, balance = 1000.0, publicKey = "123", privateKey = "123"))
        val sellerWallet = walletRepository.save(Wallet(user = seller, cryptocurrency = crypto, balance = 1000.0, publicKey = "456", privateKey = "456"))

        val orderType = typeRepository.save(OrderType("BUY"))
        val orderStatus = statusRepository.save(OrderStatus("OPEN"))

        val buyOrder = orderRepository.save(Order(
            id = 1,
            wallet = buyerWallet,
            type = orderType,
            status = orderStatus,
            unitPrice = java.math.BigDecimal("10000.00"),
            quantity = 0.1
        ))

        val sellOrder = orderRepository.save(Order(
            id = 2,
            wallet = sellerWallet,
            type = orderType,
            status = orderStatus,
            unitPrice = java.math.BigDecimal("10000.00"),
            quantity = 0.1
        ))
        val dealStatus = dealStatusRepository.save(DealStatus("INIT"))
        deal = dealRepository.save(Deal(buyOrder = buyOrder, sellOrder = sellOrder, status = dealStatus))

        priority = priorityRepository.save(Priority("HIGH", level = 1))
    }

    @Test
    fun `should save and find task by deal id`() {
        val task = taskRepository.save(Task(
            deal = deal,
            manager = manager,
            priority = priority,
            errorDescription = "None",
            result = "Success"
        ))

        val tasks = taskRepository.findByDealId(deal.id)
        assertEquals(1, tasks.size)
        assertEquals(task.id, tasks.first().id)
    }

    @Test
    fun `should find tasks by manager id`() {
        val task1 = taskRepository.save(Task(deal = deal, manager = manager, priority = priority, result = "OK"))
        val task2 = taskRepository.save(Task(deal = deal, manager = manager, priority = priority, result = "OK"))

        val tasks = taskRepository.findByManagerId(manager.id)
        assertEquals(2, tasks.size)
        assertTrue(tasks.all { it.manager?.id == manager.id })
    }

    @Test
    fun `should return empty list if no tasks for given deal`() {
        val tasks = taskRepository.findByDealId(999L)
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun `should return empty list if no tasks for given manager`() {
        val tasks = taskRepository.findByManagerId(999L)
        assertTrue(tasks.isEmpty())
    }
}
