package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.dataclasses.OrderInfo
import com.example.p2p_stock.dataclasses.TaskInfo
import com.example.p2p_stock.dataclasses.TaskPatchResultInfo
import com.example.p2p_stock.errors.IllegalAccessTaskException
import com.example.p2p_stock.errors.NotFoundTaskException
import com.example.p2p_stock.models.*
import com.example.p2p_stock.repositories.TaskRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    // Мокаем зависимости
    @Mock
    private lateinit var taskRepository: TaskRepository

    @Mock
    private lateinit var dealService: DealService

    // Внедряем моки в тестируемый сервис
    @InjectMocks
    private lateinit var taskService: TaskService

    // Тестовые данные
    private lateinit var roleUser: Role
    private lateinit var user1: User
    private lateinit var user2: User

    private lateinit var bank: Bank
    private lateinit var card1: Card
    private lateinit var card2: Card

    private lateinit var crypto: Cryptocurrency
    private lateinit var wallet1: Wallet
    private lateinit var wallet2: Wallet

    private lateinit var orderTypeBuy: OrderType
    private lateinit var orderTypeSell: OrderType
    private lateinit var orderStatus: OrderStatus
    private lateinit var buyOrder: Order
    private lateinit var sellOrder: Order

    // Статусы сделки (можно использовать для задания тестовой сделки)
    private lateinit var initialStatus: DealStatus
    private lateinit var waitingPaymentStatus: DealStatus
    private lateinit var waitingConfirmationStatus: DealStatus
    private lateinit var successStatus: DealStatus
    private lateinit var cancelledStatus: DealStatus
    private lateinit var problemStatus: DealStatus
    private lateinit var managerWaitStatus: DealStatus
    private lateinit var managerCancelledStatus: DealStatus

    private lateinit var deal: Deal

    @BeforeEach
    fun setUp() {
        // Настройка пользователей и ролей
        roleUser = Role("USER")
        user1 = User(id = 0, login = "manager1", password = "pass", email = "manager1@test.com", isActive = true, role = roleUser)
        user2 = User(id = 1, login = "manager2", password = "pass", email = "manager2@test.com", isActive = true, role = roleUser)

        // Настройка банка и карточек
        bank = Bank(name = "TestBank")
        card1 = Card(cardName = "Card1", cardNumber = "1111222233334444", bank = bank, user = user1)
        card2 = Card(cardName = "Card2", cardNumber = "5555666677778888", bank = bank, user = user2)

        // Настройка криптовалюты и кошельков
        crypto = Cryptocurrency("Bitcoin", "BTC")
        wallet1 = Wallet(user = user1, cryptocurrency = crypto, balance = 10000.0, publicKey = "123", privateKey = "123")
        wallet2 = Wallet(user = user2, cryptocurrency = crypto, balance = 10000.0, publicKey = "456", privateKey = "456")

        // Настройка параметров ордеров
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

        // Настройка статусных объектов для сделки
        initialStatus = DealStatus(name = "Приостановлено: решение проблем")
        waitingPaymentStatus = DealStatus(name = "Ожидание перевода")
        waitingConfirmationStatus = DealStatus(name = "Ожидание подтверждения перевода")
        successStatus = DealStatus(name = "Закрыто: успешно")
        cancelledStatus = DealStatus(name = "Приостановлено: решение проблем")
        problemStatus = DealStatus(name = "Ожидание решения менеджера")
        managerWaitStatus = DealStatus(name = "Закрыто: время кс истекло")
        managerCancelledStatus = DealStatus(name = "Закрыто: отменена менеджером")

        // Создание тестовой сделки, которая будет частью тестовой задачи
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
    fun `findAll returns only tasks with matching deal statuses`() {
        // Arrange: Создаем несколько задач с разными статусами сделки
        val dealStatus1 = "Приостановлено: решение проблем" // подходит
        val dealStatus2 = "Ожидание решения менеджера"       // подходит
        val dealStatusOther = "Другая ситуация"               // не подходит

        val deal1 = deal.copy().also { it.status = DealStatus(dealStatus1) }
        val deal2 = deal.copy().also { it.status = DealStatus(dealStatus2) }
        val deal3 = deal.copy().also { it.status = DealStatus(dealStatusOther) }

        val now = LocalDateTime.now()
        val task1 = Task(
            id = 1,
            deal = deal1,
            manager = null,
            confirmation = false,
            errorDescription = "Ошибка 1",
            priority = Priority("High"),
            result = "",
            updatedAt = now
        )
        val task2 = Task(
            id = 2,
            deal = deal2,
            manager = null,
            confirmation = false,
            errorDescription = "Ошибка 2",
            priority = Priority("Medium"),
            result = "",
            updatedAt = now
        )
        val task3 = Task(
            id = 3,
            deal = deal3,
            manager = null,
            confirmation = false,
            errorDescription = "Ошибка 3",
            priority = Priority("Low"),
            result = "",
            updatedAt = now
        )

        `when`(taskRepository.findAll()).thenReturn(listOf(task1, task2, task3))

        // Act
        val filteredTasks = taskService.findAll()

        // Assert: должны вернуться только task1 и task2
        assertEquals(2, filteredTasks.size)
        assertTrue(filteredTasks.contains(task1))
        assertTrue(filteredTasks.contains(task2))
        assertFalse(filteredTasks.contains(task3))
    }

    @Test
    fun `findById returns task when found`() {
        // Arrange
        val taskId = 1L
        val now = LocalDateTime.now()
        val task = Task(
            id = taskId,
            deal = deal,
            manager = null,
            confirmation = false,
            errorDescription = "",
            priority = Priority("High"),
            result = "",
            updatedAt = now
        )
        `when`(taskRepository.findById(taskId)).thenReturn(Optional.of(task))

        // Act
        val foundTask = taskService.findById(taskId)

        // Assert
        assertEquals(task, foundTask)
    }

    @Test
    fun `findById throws NotFoundTaskException when task not found`() {
        // Arrange
        val taskId = 999L
        `when`(taskRepository.findById(taskId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<NotFoundTaskException> {
            taskService.findById(taskId)
        }
        assertTrue(exception.message!!.contains("$taskId"))
    }

    @Test
    fun `save sets updatedAt and saves task`() {
        // Arrange
        val initialTime = LocalDateTime.now().minusDays(1)
        val task = Task(
            id = 1,
            deal = deal,
            manager = null,
            confirmation = false,
            errorDescription = "Ошибка",
            priority = Priority("High"),
            result = "",
            updatedAt = initialTime
        )

        val taskCaptor = ArgumentCaptor.forClass(Task::class.java)
        `when`(taskRepository.save(any(Task::class.java))).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        val savedTask = taskService.save(task)

        // Assert: проверка того, что время обновления обновлено
        verify(taskRepository, times(1)).save(taskCaptor.capture())
        val capturedTask = taskCaptor.value
        assertNotNull(capturedTask.updatedAt)
        assertTrue(capturedTask.updatedAt!!.isAfter(initialTime))
        assertEquals(capturedTask, savedTask)
    }

    @Test
    fun `delete calls repository deleteById`() {
        // Arrange
        val taskId = 5L

        // Act
        taskService.delete(taskId)

        // Assert
        verify(taskRepository, times(1)).deleteById(taskId)
    }

    @Test
    fun `takeInWork assigns manager and calls dealService managerTakeInWork`() {
        // Arrange
        val now = LocalDateTime.now()
        val task = Task(
            id = 10,
            deal = deal,
            manager = null,
            confirmation = false,
            errorDescription = "",
            priority = Priority("High"),
            result = "",
            updatedAt = now
        )

        // Заглушка для сохранения
        `when`(taskRepository.save(any(Task::class.java))).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        val updatedTask = taskService.takeInWork(user1, task)

        // Assert: проверяем что dealService.managerTakeInWork вызван и менеджер назначен
        verify(dealService, times(1)).managerTakeInWork(user1, deal)
        assertEquals(user1, updatedTask.manager)
    }

    @Test
    fun `denyDealTask throws IllegalAccessTaskException when manager is null`() {
        // Arrange
        val task = Task(
            id = 20,
            deal = deal,
            manager = null, // менеджер отсутствует
            confirmation = false,
            errorDescription = "",
            priority = Priority("High"),
            result = "",
            updatedAt = LocalDateTime.now()
        )
        val resultPatch = TaskPatchResultInfo(result = "Отклонено")

        // Act & Assert
        assertThrows<IllegalAccessTaskException> {
            taskService.denyDealTask(task, resultPatch)
        }
    }

    @Test
    fun `denyDealTask sets confirmation false, updates result and calls dealService managerReject`() {
        // Arrange
        val task = Task(
            id = 30,
            deal = deal,
            manager = user2,
            confirmation = true, // изначально установлено в true
            errorDescription = "Некоторая ошибка",
            priority = Priority("Medium"),
            result = "",
            updatedAt = LocalDateTime.now()
        )
        val resultPatch = TaskPatchResultInfo(result = "Отклонено")

        `when`(taskRepository.save(any(Task::class.java))).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        val updatedTask = taskService.denyDealTask(task, resultPatch)

        // Assert
        assertFalse(updatedTask.confirmation == true)
        assertEquals("Отклонено", updatedTask.result)
        verify(dealService, times(1)).managerReject(task.deal!!)
    }

    @Test
    fun `acceptDealTask throws IllegalAccessTaskException when manager is null`() {
        // Arrange
        val task = Task(
            id = 40,
            deal = deal,
            manager = null,
            confirmation = false,
            errorDescription = "",
            priority = Priority("Low"),
            result = "",
            updatedAt = LocalDateTime.now()
        )
        val resultPatch = TaskPatchResultInfo(result = "Принято")

        // Act & Assert
        assertThrows<IllegalAccessTaskException> {
            taskService.acceptDealTask(task, resultPatch)
        }
    }

    @Test
    fun `acceptDealTask sets confirmation true, updates result and calls dealService managerApprove`() {
        // Arrange
        val task = Task(
            id = 50,
            deal = deal,
            manager = user2,
            confirmation = false,
            errorDescription = "Ошибка",
            priority = Priority("Medium"),
            result = "",
            updatedAt = LocalDateTime.now()
        )
        val resultPatch = TaskPatchResultInfo(result = "Принято")

        `when`(taskRepository.save(any(Task::class.java))).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        val updatedTask = taskService.acceptDealTask(task, resultPatch)

        // Assert
        assertTrue(updatedTask.confirmation == true)
        assertEquals("Принято", updatedTask.result)
        verify(dealService, times(1)).managerApprove(task.deal!!)
    }

    @Test
    fun `taskToTaskInfo returns correctly mapped TaskInfo`() {
        // Arrange
        val now = LocalDateTime.now()
        val task = Task(
            id = 60,
            deal = deal,
            manager = user1,
            confirmation = true,
            errorDescription = "Нет ошибок",
            priority = Priority("High"),
            result = "Успешно",
            updatedAt = now
        )
        // Заглушка для маппинга сделки в dealInfo
        val dealInfoStub = dealToDealInfo(deal)
        `when`(dealService.dealToDealInfo(deal)).thenReturn(dealInfoStub)

        // Act
        val taskInfo: TaskInfo = taskService.taskToTaskInfo(task)

        // Assert
        assertEquals(task.id, taskInfo.id)
        assertEquals(user1.login, taskInfo.managerLogin)
        assertEquals(task.confirmation, taskInfo.confirmation)
        assertEquals(task.errorDescription, taskInfo.errorDescription)
        assertEquals(task.priority?.name, taskInfo.priorityName)
        assertEquals(task.result, taskInfo.result)
        assertEquals(task.updatedAt.toString(), taskInfo.updatedAt)
    }

    fun orderToOrderInfo(order: Order): OrderInfo = OrderInfo(
        id = order.id,
        userLogin = order.wallet?.user?.login?:"",
        walletId = order.wallet!!.id,
        cryptocurrencyCode = order.wallet?.cryptocurrency?.code ?: "",
        cardId = order.card?.id?:0,
        typeName = order.type!!.name,
        statusName = order.status!!.name,
        unitPrice = order.unitPrice,
        quantity = order.quantity,
        description = order.description,
        createdAt = order.createdAt.toString(),
        lastStatusChange = (order.lastStatusChange ?: order.createdAt).toString(),
    )

    fun dealToDealInfo(deal: Deal): DealInfo =
        DealInfo(
            id = deal.id,
            buyOrder = orderToOrderInfo(deal.buyOrder!!),
            sellOrder = orderToOrderInfo(deal.sellOrder!!),
            statusName = deal.status!!.name,
            createdAt = deal.createdAt.toString(),
            lastStatusChange = deal.lastStatusChange.toString()
        )
}
