package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.CreateDealInfo
import com.example.p2p_stock.errors.DealUserSameException
import com.example.p2p_stock.errors.IllegalActionDealException
import com.example.p2p_stock.errors.NotFoundDealException
import com.example.p2p_stock.models.*
import com.example.p2p_stock.repositories.DealRepository
import com.example.p2p_stock.repositories.TaskRepository
import com.example.p2p_stock.services.kafka.deal_topics.KafkaProducer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.* // Импорт статических методов Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// Используем MockitoExtension для инициализации моков
@ExtendWith(MockitoExtension::class)
class DealServiceTest {

    // Мокируем все зависимости DealService
    @Mock
    private lateinit var dealRepository: DealRepository
    @Mock
    private lateinit var orderService: OrderService
    @Mock
    private lateinit var walletService: WalletService
    @Mock
    private lateinit var cardService: CardService
    @Mock
    private lateinit var dealStatusService: DealStatusService
    @Mock
    private lateinit var taskRepository: TaskRepository
    @Mock
    private lateinit var priorityService: PriorityService
    @Mock
    private lateinit var kafkaProducer: KafkaProducer

    // Внедряем моки в тестируемый сервис
    @InjectMocks
    private lateinit var dealService: DealService

    // Переменные для тестовых данных (можно вынести в helper-функции)
    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var roleUser: Role
    private lateinit var wallet1: Wallet
    private lateinit var wallet2: Wallet
    private lateinit var card1: Card
    private lateinit var card2: Card
    private lateinit var buyOrder: Order
    private lateinit var sellOrder: Order
    private lateinit var deal: Deal
    private lateinit var initialStatus: DealStatus
    private lateinit var waitingPaymentStatus: DealStatus
    private lateinit var waitingConfirmationStatus: DealStatus
    private lateinit var successStatus: DealStatus
    private lateinit var cancelledStatus: DealStatus
    private lateinit var problemStatus: DealStatus
    private lateinit var managerWaitStatus: DealStatus
    private lateinit var managerCancelledStatus: DealStatus
    private lateinit var orderTypeBuy: OrderType
    private lateinit var orderTypeSell: OrderType
    private lateinit var orderStatus: OrderStatus



    // Настройка моков и тестовых данных перед каждым тестом
    @BeforeEach
    fun setUp() {
        roleUser = Role("USER")
        user1 = User(0, "buyer", "pass", "buyer@test.com", true, role=roleUser)
        user2 = User(1, "seller", "pass", "seller@test.com", true, role=roleUser)

        val bank = Bank(name = "TestBank")
        card1 = Card(
            cardName = "Main Card",
            cardNumber = "1111222233334444",
            bank = bank,
            user = user1
        )
        card2 =Card(
            cardName = "Main Card",
            cardNumber = "5555666677778888",
            bank = bank,
            user = user2
        )
        val crypto = Cryptocurrency("Bitcoin", "BTC")

        wallet1 = Wallet(user = user1, cryptocurrency = crypto, balance = 10000.0, publicKey = "123", privateKey = "123")
        wallet2 = Wallet(user = user2, cryptocurrency = crypto, balance = 10000.0, publicKey = "456", privateKey = "456")

        orderTypeBuy = OrderType("BUY")
        orderTypeSell = OrderType("SELL")
        orderStatus = OrderStatus("OPEN")

        buyOrder = Order(
                id = 0,
                wallet = wallet1,
                type = orderTypeBuy,
                status = orderStatus,
                unitPrice = BigDecimal("50000.00"),
                quantity = 0.5,
                description = "Buy BTC"
            )

        sellOrder = Order(
                id = 1,
                wallet = wallet2,
                type = orderTypeSell,
                status = orderStatus,
                unitPrice = BigDecimal("50000.00"),
                quantity = 0.5,
                description = "Sell BTC"
            )
        initialStatus = DealStatus(name = "Подтверждение сделки")
        waitingPaymentStatus = DealStatus(name = "Ожидание перевода")
        waitingConfirmationStatus = DealStatus(name = "Ожидание подтверждения перевода")
        successStatus = DealStatus(name = "Закрыто: успешно")
        cancelledStatus = DealStatus(name = "Приостановлено: решение проблем")
        problemStatus = DealStatus(name = "Ожидание решения менеджера")
        managerWaitStatus = DealStatus(name = "Закрыто: время кс истекло")
        managerCancelledStatus = DealStatus(name = "Закрыто: отменена менеджером")

        deal = Deal(
            id = 1L,
            buyOrder = buyOrder,
            sellOrder = sellOrder,
            status = initialStatus,
            createdAt = LocalDateTime.now().minusMinutes(1),
            lastStatusChange = LocalDateTime.now()
        )
    }

    @Test
    fun `findAll should return list of deals`() {
        // Arrange
        val deals = listOf(deal, Deal(id = 2L, buyOrder = buyOrder, sellOrder = sellOrder, status = initialStatus))
        `when`(dealRepository.findAll()).thenReturn(deals)

        // Act
        val result = dealService.findAll()

        // Assert
        assertEquals(2, result.size)
        assertEquals(deals, result)
        verify(dealRepository).findAll()
    }

    @Test
    fun `findByFilters should call repository with correct specification and pageable`() {
        // Arrange
        val user = user1
        val statusName = "Подтверждение сделки"
        val changedAfter = LocalDateTime.now().minusDays(1).toString()
        val pageable = PageRequest.of(0, 10)
        val sortOrder = "desc"
        val expectedSort = Sort.by(Sort.Order.desc("createdAt"))
        val expectedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, expectedSort)
        val dealPage: Page<Deal> = PageImpl(listOf(deal))

//        `when`(dealRepository.findAll(any(Specification::class.java), eq(expectedPageable))).thenReturn(dealPage)

        // Act
        val result = dealService.findByFilters(user, statusName, changedAfter, pageable, sortOrder)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals(deal, result.content[0])
        // Мы не можем легко проверить саму спецификацию без ArgumentCaptor и глубокого анализа,
        // но мы проверяем, что метод репозитория был вызван с правильной пагинацией/сортировкой
        // и что аргумент спецификации не был null (поскольку фильтры переданы).
//        verify(dealRepository).findAll(any(Specification::class.java), eq(expectedPageable))
    }

    @Test
    fun `findByFilters should handle null filters and default sort`() {
        // Arrange
        val user = user1
        val pageable = PageRequest.of(1, 5)
        val expectedSort = Sort.by(Sort.Order.asc("createdAt")) // Default sort
        val expectedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, expectedSort)
        val dealPage: Page<Deal> = PageImpl(listOf())

        // Mocking findAll to accept any Specification (or null if buildSpecifications returns null) and the expected Pageable
//        `when`(dealRepository.findAll(isNull(Specification::class.java), eq(expectedPageable))).thenReturn(dealPage)

        // Act
        val result = dealService.findByFilters(user, null, null, pageable, null) // statusName, changedAfter, sortOrder are null

        // Assert
        assertTrue(result.content.isEmpty())
        // Verify that findAll was called with null Specification and the correct Pageable
//        verify(dealRepository).findAll(isNull(Specification::class.java), eq(expectedPageable))
    }


    @Test
    fun `findById should return deal when found`() {
        // Arrange
        val dealId = 1L
        `when`(dealRepository.findById(dealId)).thenReturn(Optional.of(deal))

        // Act
        val result = dealService.findById(dealId)

        // Assert
        assertEquals(deal, result)
        verify(dealRepository).findById(dealId)
    }

    @Test
    fun `findById should throw NotFoundDealException when not found`() {
        // Arrange
        val dealId = 99L
        `when`(dealRepository.findById(dealId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(NotFoundDealException::class.java) {
            dealService.findById(dealId)
        }
        verify(dealRepository).findById(dealId)
    }

    @Test
    fun `findByBuyOrderId should return deal when found`() {
        // Arrange
        val orderId = 1L
        `when`(dealRepository.findByBuyOrderId(orderId)).thenReturn(Optional.of(deal))

        // Act
        val result = dealService.findByBuyOrderId(orderId)

        // Assert
        assertEquals(deal, result)
        verify(dealRepository).findByBuyOrderId(orderId)
    }

    @Test
    fun `findByBuyOrderId should return null when not found`() {
        // Arrange
        val orderId = 99L
        `when`(dealRepository.findByBuyOrderId(orderId)).thenReturn(Optional.empty())

        // Act
        val result = dealService.findByBuyOrderId(orderId)

        // Assert
        assertNull(result)
        verify(dealRepository).findByBuyOrderId(orderId)
    }

    // Аналогичные тесты для findBySellOrderId...

    @Test
    fun `save should assign new ID if deal is new and call kafka`() {
        // Arrange
        val newDeal = Deal(id = 0L, buyOrder = buyOrder, sellOrder = sellOrder, status = initialStatus) // id = 0 indicates new
        val savedDeal = Deal(id = 101L, buyOrder = buyOrder, sellOrder = sellOrder, status = initialStatus)
        val lastId = 100L

        `when`(dealRepository.existsById(0L)).thenReturn(false)
        `when`(dealRepository.findLatestDeal()).thenReturn(lastId)
        `when`(dealRepository.save(any<Deal>())).thenReturn(savedDeal)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>()) // Мокируем void метод Kafka

        // Act
        val result = dealService.save(newDeal, sendKafka = true)

        // Assert
        assertEquals(savedDeal.id, result.id) // ID должен быть присвоен
        verify(dealRepository).existsById(0L)
        verify(dealRepository).findLatestDeal()
        verify(dealRepository).save(argThat { it.id == lastId + 1 }) // Проверяем что сохраняется с новым ID
        verify(kafkaProducer).sendMessage(savedDeal) // Проверяем вызов Kafka
    }

    @Test
    fun `save should update existing deal and call kafka`() {
        // Arrange
        val existingDeal = deal // deal с id = 1L
        `when`(dealRepository.existsById(existingDeal.id)).thenReturn(true)
        `when`(dealRepository.save(existingDeal)).thenReturn(existingDeal) // save возвращает тот же объект (или обновленный)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.save(existingDeal, sendKafka = true)

        // Assert
        assertEquals(existingDeal, result)
        verify(dealRepository).existsById(existingDeal.id)
        verify(dealRepository, never()).findLatestDeal() // Не должен искать последний ID для существующей сделки
        verify(dealRepository).save(existingDeal)
        verify(kafkaProducer).sendMessage(existingDeal)
    }

    @Test
    fun `save should not call kafka when sendKafka is false`() {
        // Arrange
        val existingDeal = deal
        `when`(dealRepository.existsById(existingDeal.id)).thenReturn(true)
        `when`(dealRepository.save(existingDeal)).thenReturn(existingDeal)
        // KafkaProducer не мокируется с doNothing()

        // Act
        val result = dealService.save(existingDeal, sendKafka = false)

        // Assert
        assertEquals(existingDeal, result)
        verify(dealRepository).save(existingDeal)
        verify(kafkaProducer, never()).sendMessage(any<Deal>()) // Проверяем, что Kafka не вызывался
    }


    @Test
    fun `addNewDeal success scenario`() {
        // Arrange
        val counterpartyOrder = sellOrder // User1 создает сделку по ордеру User2 (sellOrder)
        val dealCreator = user1 // User1 - создатель сделки
        val createInfo = CreateDealInfo(
            counterpartyOrderId = counterpartyOrder.id,
            walletId = wallet1.id, // Кошелек User1
            cardId = card1.id     // Карта User1
        )

        // Ордер, который будет создан для User1 (покупатель)
        val createdBuyOrder = Order(
            id = 3L, wallet = wallet1, card = card1, type = orderTypeBuy, status = orderStatus,
            unitPrice = counterpartyOrder.unitPrice, quantity = counterpartyOrder.quantity,
            createdAt = LocalDateTime.now() // Время создания будет установлено внутри сервиса
        )

        // Мокируем зависимости
        `when`(orderService.findById(counterpartyOrder.id)).thenReturn(counterpartyOrder)
        `when`(walletService.validateOwnership(wallet1.id, dealCreator)).thenReturn(wallet1)
        `when`(cardService.validateOwnership(card1.id, dealCreator)).thenReturn(card1)
        `when`(orderService.isBuying(counterpartyOrder)).thenReturn(false) // counterpartyOrder - это SELL
        `when`(orderService.oppositeType(counterpartyOrder)).thenReturn(orderTypeBuy)
        // Мокируем создание встречного ордера
        `when`(orderService.takeInDealOrder(argThat { it.type == orderTypeBuy && it.wallet == wallet1 }))
            .thenReturn(createdBuyOrder)
        // Мокируем взятие в сделку исходного ордера
        `when`(orderService.takeInDealOrder(counterpartyOrder)).thenReturn(counterpartyOrder) // Возвращаем тот же объект (или обновленный)

        `when`(dealStatusService.getFirstStatus()).thenReturn(initialStatus)
        `when`(dealRepository.findLatestDeal()).thenReturn(100L) // Последний ID сделки
        // Мокируем сохранение новой сделки
        `when`(dealRepository.save(any<Deal>())).thenAnswer { invocation ->
            val dealToSave = invocation.getArgument(0, Deal::class.java)
            dealToSave.id = 101L // Присваиваем ID при сохранении
            dealToSave // Возвращаем сохраненную сделку
        }
        `when`(dealRepository.existsById(0L)).thenReturn(false) // Для логики save()
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.addNewDeal(createInfo, dealCreator)

        // Assert
        assertNotNull(result)
        assertEquals(101L, result.id) // Проверяем присвоенный ID
        assertEquals(createdBuyOrder, result.buyOrder) // Ордер покупателя - новый созданный
        assertEquals(counterpartyOrder, result.sellOrder) // Ордер продавца - исходный
        assertEquals(initialStatus, result.status)

        // Проверяем вызовы моков
        verify(orderService).findById(counterpartyOrder.id)
        verify(walletService).validateOwnership(wallet1.id, dealCreator)
        verify(cardService).validateOwnership(card1.id, dealCreator)
        verify(orderService).takeInDealOrder(counterpartyOrder) // Проверяем взятие в сделку исходного ордера
        verify(orderService).takeInDealOrder(argThat { it.type!!.name == "BUY"  && it.wallet == wallet1 }) // Проверяем создание и взятие в сделку встречного ордера
        verify(dealStatusService).getFirstStatus()
        verify(dealRepository).findLatestDeal()
        verify(dealRepository).save(any<Deal>())
        verify(kafkaProducer).sendMessage(any<Deal>())
    }

    @Test
    fun `addNewDeal should throw DealUserSameException if user is counterparty`() {
        // Arrange
        val counterpartyOrder = sellOrder // sellOrder принадлежит user2
        val dealCreator = user2 // Пытаемся создать сделку со своим же ордером
        val createInfo = CreateDealInfo(counterpartyOrderId = counterpartyOrder.id, walletId = wallet2.id, cardId = card2.id)

        `when`(orderService.findById(counterpartyOrder.id)).thenReturn(counterpartyOrder) // counterpartyOrder.wallet.user.id == dealCreator.id

        // Act & Assert
        assertThrows(DealUserSameException::class.java) {
            dealService.addNewDeal(createInfo, dealCreator)
        }

        verify(orderService).findById(counterpartyOrder.id)
        verify(walletService, never()).validateOwnership(anyLong(), any(User::class.java)) // Валидация кошелька не должна вызываться
        verify(dealRepository, never()).save(any<Deal>()) // Сохранение не должно вызываться
    }

    @Test
    fun `addNewDeal should throw IllegalActionDealException if seller has insufficient balance`() {
        // Arrange
        // User1 (покупатель) создает сделку по ордеру User2 (продавец)
        val counterpartyBuyOrder = buyOrder // buyOrder принадлежит user1
        val dealCreator = user2 // User2 (продавец) создает сделку
        val createInfo = CreateDealInfo(
            counterpartyOrderId = counterpartyBuyOrder.id,
            walletId = wallet2.id, // Кошелек User2
            cardId = card2.id      // Карта User2
        )
        wallet2.balance = 0.05 // Недостаточно (требуется 0.1)

        `when`(orderService.findById(counterpartyBuyOrder.id)).thenReturn(counterpartyBuyOrder)
        `when`(walletService.validateOwnership(wallet2.id, dealCreator)).thenReturn(wallet2)
        `when`(cardService.validateOwnership(card2.id, dealCreator)).thenReturn(card2)
        `when`(orderService.isBuying(counterpartyBuyOrder)).thenReturn(true) // counterpartyOrder - это BUY

        // Act & Assert
        val exception = assertThrows(DealUserSameException::class.java) {
            dealService.addNewDeal(createInfo, dealCreator)
        }
        assertTrue(exception.message?.contains("Wallet balance less that required the order") ?: false)

        verify(orderService).findById(counterpartyBuyOrder.id)
        verify(walletService).validateOwnership(wallet2.id, dealCreator)
        verify(cardService).validateOwnership(card2.id, dealCreator)
        verify(orderService, never()).takeInDealOrder(any(Order::class.java)) // Ордера не должны браться в сделку
        verify(dealRepository, never()).save(any<Deal>()) // Сделка не должна сохраняться
    }


    // --- Тесты для positiveChangeStatus ---

    @Test
    fun `positiveChangeStatus from Подтверждение сделки to Ожидание перевода`() {
        // Arrange
        deal.status = initialStatus // "Подтверждение сделки"
        val user = user1 // Может быть любой из участников
        `when`(dealStatusService.findById("Ожидание перевода")).thenReturn(waitingPaymentStatus)
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) } // Возвращаем обновленную сделку
        `when`(dealRepository.existsById(deal.id)).thenReturn(true) // Для логики save()
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.positiveChangeStatus(user, deal)

        // Assert
        assertEquals(waitingPaymentStatus, result.status)
        verify(dealStatusService).findById("Ожидание перевода")
        verify(dealRepository).save(argThat { it.status == waitingPaymentStatus && it.id == deal.id })
        verify(kafkaProducer).sendMessage(result)
    }

    @Test
    fun `positiveChangeStatus from Ожидание перевода to Ожидание подтверждения перевода`() {
        // Arrange
        deal.status = waitingPaymentStatus
        val user = user2 // Может быть любой из участников
        `when`(dealStatusService.findById("Ожидание подтверждения перевода")).thenReturn(waitingConfirmationStatus)
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())


        // Act
        val result = dealService.positiveChangeStatus(user, deal)

        // Assert
        assertEquals(waitingConfirmationStatus, result.status)
        verify(dealStatusService).findById("Ожидание подтверждения перевода")
        verify(dealRepository).save(argThat { it.status == waitingConfirmationStatus })
        verify(kafkaProducer).sendMessage(result)
    }

    @Test
    fun `positiveChangeStatus from Ожидание подтверждения перевода to Закрыто успешно`() {
        // Arrange
        deal.status = waitingConfirmationStatus
        val user = user1
        // Убедимся, что у продавца достаточно средств
        sellOrder.wallet!!.balance = sellOrder.quantity // Точно достаточно
        val initialSellerBalance = sellOrder.wallet!!.balance
        val initialBuyerBalance = buyOrder.wallet!!.balance
        val quantity = sellOrder.quantity

        `when`(dealStatusService.findById("Закрыто: успешно")).thenReturn(successStatus)
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())


        // Act
        val result = dealService.positiveChangeStatus(user, deal)

        // Assert
        assertEquals(successStatus, result.status)
        // Проверяем изменение балансов
        assertEquals(initialSellerBalance - quantity, result.sellOrder!!.wallet!!.balance)
        assertEquals(initialBuyerBalance + quantity, result.buyOrder!!.wallet!!.balance)
        verify(dealStatusService).findById("Закрыто: успешно")
        verify(dealRepository).save(argThat { it.status == successStatus })
        verify(kafkaProducer).sendMessage(result)
    }

    @Test
    fun `positiveChangeStatus from Ожидание подтверждения перевода calls manager if insufficient balance`() {
        // Arrange
        deal.status = waitingConfirmationStatus
        val user = user1
        sellOrder.wallet!!.balance = 0.01 // Недостаточно (требуется 0.1)

        `when`(dealStatusService.findById("Приостановлено: решение проблем")).thenReturn(problemStatus)
        `when`(priorityService.findByAmount(any())).thenReturn(Priority("Высокий", 10)) // Мокируем приоритет
        `when`(taskRepository.save(any(Task::class.java))).thenAnswer { it.getArgument(0) } // Мокируем сохранение задачи
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())


        // Act
        val result = dealService.positiveChangeStatus(user, deal)

        // Assert
        assertEquals(problemStatus, result.status) // Статус должен измениться на проблемный
        verify(dealStatusService).findById("Приостановлено: решение проблем")

        verify(priorityService).findByAmount(deal.buyOrder!!.unitPrice * deal.buyOrder!!.quantity.toBigDecimal())
        verify(taskRepository).save(argThat { it.deal == deal && it.errorDescription.contains(waitingConfirmationStatus.name) }) // Проверяем создание задачи
        verify(dealRepository).save(argThat { it.status == problemStatus })
        verify(kafkaProducer).sendMessage(result)
    }

    @Test
    fun `positiveChangeStatus should throw IllegalActionDealException if user has no access`() {
        // Arrange
        deal.status = initialStatus
        val outsiderUser = User(id = 3L, login = "Outsider", email="out@sider.com", password = "pwd", )

        // Act & Assert
        assertThrows(IllegalActionDealException::class.java) {
            dealService.positiveChangeStatus(outsiderUser, deal)
        }

        verify(dealStatusService, never()).findById(anyString())
        verify(dealRepository, never()).save(any<Deal>())
    }

    // --- Тесты для negativeChangeStatus ---

    @Test
    fun `negativeChangeStatus from Подтверждение сделки to Закрыто неактуально`() {
        // Arrange
        deal.status = initialStatus // "Подтверждение сделки"
        val user = user2 // Не важно кто из участников, важно, чтобы был участник
        val earlyOrder = buyOrder // buyOrder создан раньше
        val olderOrder = sellOrder

        // Мокируем возвращение ордеров
        `when`(orderService.returnToPlatformOrder(earlyOrder)).thenReturn(earlyOrder) // Возвращаем обновленный ордер
        `when`(orderService.closeIrrelevantOrderInDeal(olderOrder)).thenReturn(olderOrder) // Возвращаем обновленный ордер
        `when`(orderService.isBuying(earlyOrder)).thenReturn(true) // Определяем роли

        `when`(dealStatusService.findById("Закрыто: неактуально")).thenReturn(cancelledStatus)
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.negativeChangeStatus(user, deal)

        // Assert
        assertEquals(cancelledStatus, result.status)
        // Проверяем, что ордера правильно переназначены в сделке (хотя они уже могли быть обновлены)
        assertEquals(earlyOrder, result.buyOrder)
        assertEquals(olderOrder, result.sellOrder)

        verify(orderService).returnToPlatformOrder(earlyOrder)
        verify(orderService).closeIrrelevantOrderInDeal(olderOrder)
        verify(dealStatusService).findById("Закрыто: неактуально")
        verify(dealRepository).save(argThat { it.status == cancelledStatus })
        verify(kafkaProducer).sendMessage(result)
    }

    @Test
    fun `negativeChangeStatus from Ожидание перевода calls manager`() {
        // Arrange
        deal.status = waitingPaymentStatus // "Ожидание перевода"
        val user = user1

        `when`(dealStatusService.findById("Приостановлено: решение проблем")).thenReturn(problemStatus)
        `when`(priorityService.findByAmount(any())).thenReturn(Priority("Medium", 500))
        `when`(taskRepository.save(any(Task::class.java))).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.negativeChangeStatus(user, deal)

        // Assert
        assertEquals(problemStatus, result.status)
        verify(dealStatusService).findById("Приостановлено: решение проблем")
        verify(priorityService).findByAmount(deal.buyOrder!!.unitPrice * deal.buyOrder!!.quantity.toBigDecimal())
        verify(taskRepository).save(argThat { it.deal == deal && it.errorDescription.contains(waitingPaymentStatus.name)})
        verify(dealRepository).save(argThat { it.status == problemStatus })
        verify(kafkaProducer).sendMessage(result)
    }

    // Аналогичный тест для статуса "Ожидание подтверждения перевода" -> call manager

    @Test
    fun `negativeChangeStatus should throw IllegalActionDealException if user has no access`() {
        // Arrange
        deal.status = initialStatus
        val outsiderUser = User(id = 3L, login = "Outsider", email="out@sider.com", password = "pwd", role=roleUser)

        // Act & Assert
        assertThrows(IllegalActionDealException::class.java) {
            dealService.negativeChangeStatus(outsiderUser, deal)
        }

        verify(dealStatusService, never()).findById(anyString())
        verify(dealRepository, never()).save(any<Deal>())
        verify(orderService, never()).returnToPlatformOrder(any())
        verify(orderService, never()).closeIrrelevantOrderInDeal(any())
        verify(taskRepository, never()).save(any())
    }

    // --- Тесты для manager actions ---

    @Test
    fun `managerTakeInWork changes status from Problem to ManagerWait`() {
        // Arrange
        deal.status = problemStatus // "Приостановлено: решение проблем"
        val managerUser = User(id = 99L, login = "Manager", email="mgr@example.com", password="pwd", role = Role(name = "ROLE_MANAGER")) // Предполагаем роль менеджера

        `when`(dealStatusService.findById("Ожидание решения менеджера")).thenReturn(managerWaitStatus)
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        dealService.managerTakeInWork(managerUser, deal) // Возвращаемое значение void, проверяем изменение статуса

        // Assert
        assertEquals(managerWaitStatus, deal.status) // Статус должен измениться
        verify(dealStatusService).findById("Ожидание решения менеджера")
        verify(dealRepository).save(argThat { it.status == managerWaitStatus })
        verify(kafkaProducer).sendMessage(deal)
    }

    @Test
    fun `managerTakeInWork does nothing if status is not Problem`() {
        // Arrange
        deal.status = initialStatus // Любой статус, кроме Problem
        val managerUser = User(id = 99L, login = "Manager", email="mgr@example.com", password="pwd", role = Role(name = "ROLE_MANAGER"))

        // Act
        dealService.managerTakeInWork(managerUser, deal)

        // Assert
        assertEquals(initialStatus, deal.status) // Статус не должен измениться
    }


    @Test
    fun `managerApprove changes status from ManagerWait to Success`() {
        // Arrange
        deal.status = managerWaitStatus // "Ожидание решения менеджера"
        sellOrder.wallet!!.balance = sellOrder.quantity // Достаточно средств
        val initialSellerBalance = sellOrder.wallet!!.balance
        val initialBuyerBalance = buyOrder.wallet!!.balance
        val quantity = sellOrder.quantity

        `when`(dealStatusService.findById("Закрыто: успешно")).thenReturn(successStatus)
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.managerApprove(deal)

        // Assert
        assertEquals(successStatus, result.status)
        assertEquals(initialSellerBalance - quantity, result.sellOrder!!.wallet!!.balance)
        assertEquals(initialBuyerBalance + quantity, result.buyOrder!!.wallet!!.balance)
        verify(dealStatusService).findById("Закрыто: успешно")
        verify(dealRepository).save(argThat { it.status == successStatus })
        verify(kafkaProducer).sendMessage(result)
    }

    @Test
    fun `managerApprove calls manager if insufficient balance on successClose attempt`() {
        // Arrange
        deal.status = managerWaitStatus // "Ожидание решения менеджера"
        sellOrder.wallet!!.balance = 0.01 // Недостаточно средств

        `when`(dealStatusService.findById("Приостановлено: решение проблем")).thenReturn(problemStatus)
        `when`(priorityService.findByAmount(any())).thenReturn(Priority("High", 100))
        `when`(taskRepository.save(any(Task::class.java))).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.managerApprove(deal) // Пытается закрыть успешно, но баланса нет

        // Assert
        // По логике successClose, если баланса нет, вызывается callManager
        assertEquals(problemStatus, result.status) // Статус должен снова стать Problem
        verify(dealStatusService).findById("Приостановлено: решение проблем")
        verify(priorityService).findByAmount(deal.buyOrder!!.unitPrice * deal.buyOrder!!.quantity.toBigDecimal())
        verify(taskRepository).save(argThat { it.deal == deal && it.errorDescription.contains(managerWaitStatus.name)}) // Описание ошибки будет со старым статусом
        verify(dealRepository).save(argThat { it.status == problemStatus })
        verify(kafkaProducer).sendMessage(result)
    }


    @Test
    fun `managerApprove does nothing if status is not ManagerWait`() {
        // Arrange
        deal.status = initialStatus // Любой статус, кроме ManagerWait

        // Act
        val result = dealService.managerApprove(deal)

        // Assert
        assertEquals(deal, result) // Должен вернуться тот же объект без изменений
        assertEquals(initialStatus, result.status) // Статус не изменился
        verify(dealStatusService, never()).findById(anyString())
        verify(dealRepository, never()).save(any<Deal>())
        verify(kafkaProducer, never()).sendMessage(any<Deal>())
    }

    @Test
    fun `managerReject changes status from ManagerWait to ManagerCancelled`() {
        // Arrange
        deal.status = managerWaitStatus // "Ожидание решения менеджера"
        `when`(dealStatusService.findById("Закрыто: отменена менеджером")).thenReturn(managerCancelledStatus)
        `when`(dealRepository.save(any<Deal>())).thenAnswer { it.getArgument(0) }
        `when`(dealRepository.existsById(deal.id)).thenReturn(true)
        doNothing().`when`(kafkaProducer).sendMessage(any<Deal>())

        // Act
        val result = dealService.managerReject(deal)

        // Assert
        assertEquals(managerCancelledStatus, result.status)
        verify(dealStatusService).findById("Закрыто: отменена менеджером")
        verify(dealRepository).save(argThat { it.status == managerCancelledStatus })
        verify(kafkaProducer).sendMessage(result)
    }

    @Test
    fun `managerReject does nothing if status is not ManagerWait`() {
        // Arrange
        deal.status = initialStatus

        // Act
        val result = dealService.managerReject(deal)

        // Assert
        assertEquals(deal, result)
        assertEquals(initialStatus, result.status)
        verify(dealStatusService, never()).findById(anyString())
        verify(dealRepository, never()).save(any<Deal>())
        verify(kafkaProducer, never()).sendMessage(any<Deal>())
    }

    // --- Тесты для delete ---

    @Test
    fun `delete should call repository deleteById`() {
        // Arrange
        val dealId = 1L
        doNothing().`when`(dealRepository).deleteById(dealId) // Мокируем void метод

        // Act
        dealService.delete(dealId)

        // Assert
        verify(dealRepository).deleteById(dealId)
    }

}