package com.example.p2p_stock.services

import OrderSpecifications // Предполагаемый импорт, если это отдельный класс/object
import com.example.p2p_stock.dataclasses.CreateOrderInfo
import com.example.p2p_stock.dataclasses.OrderInfo
import com.example.p2p_stock.errors.IllegalActionOrderException
import com.example.p2p_stock.errors.NotFoundOrderException
import com.example.p2p_stock.errors.NotFoundWalletException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.* // Import models needed
import com.example.p2p_stock.repositories.OrderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.* // All mockito-kotlin helpers
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {

    @Mock
    private lateinit var orderRepository: OrderRepository
    @Mock
    private lateinit var walletService: WalletService
    @Mock
    private lateinit var cardService: CardService
    @Mock
    private lateinit var orderTypeService: OrderTypeService
    @Mock
    private lateinit var orderStatusService: OrderStatusService

    @InjectMocks
    private lateinit var orderService: OrderService

    @Captor
    private lateinit var orderCaptor: ArgumentCaptor<Order>
    @Captor
    private lateinit var specificationCaptor: ArgumentCaptor<Specification<Order>>
    @Captor
    private lateinit var pageableCaptor: ArgumentCaptor<Pageable>


    // Тестовые данные
    private lateinit var testUser: User
    private lateinit var testWallet: Wallet
    private lateinit var testCard: Card
    private lateinit var testCrypto: Cryptocurrency
    private lateinit var testBank: Bank
    private lateinit var testOrderTypeBuy: OrderType
    private lateinit var testOrderTypeSell: OrderType
    private lateinit var testStatusModeration: OrderStatus
    private lateinit var testStatusAvailable: OrderStatus
    private lateinit var testStatusInDeal: OrderStatus
    private lateinit var testStatusClosedSuccess: OrderStatus
    private lateinit var testStatusClosedProblem: OrderStatus
    private lateinit var testStatusClosedIrrelevant: OrderStatus

    private lateinit var testOrder: Order
    private lateinit var createOrderInfo: CreateOrderInfo

    private val now: LocalDateTime = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        testUser = User(id = 1L, login = "tester")
        testCrypto = Cryptocurrency(code = "BTC")
        testBank = Bank(name = "TestBank")
        testWallet = Wallet(id = 100L, user = testUser, cryptocurrency = testCrypto)
        testCard = Card(id = 200L, user = testUser, bank = testBank)

        testOrderTypeBuy = OrderType(name = "Покупка")
        testOrderTypeSell = OrderType(name = "Продажа")
        testStatusModeration = OrderStatus(name = "Модерация")
        testStatusAvailable = OrderStatus(name = "Доступна на платформе")
        testStatusInDeal = OrderStatus(name = "Используется в сделке")
        testStatusClosedSuccess = OrderStatus(name = "Закрыто: успешно")
        testStatusClosedProblem = OrderStatus(name = "Закрыто: проблема")
        testStatusClosedIrrelevant = OrderStatus(name = "Закрыто: неактуально")


        testOrder = Order(
            id = 500L,
            wallet = testWallet,
            card = testCard,
            type = testOrderTypeBuy,
            status = testStatusModeration,
            unitPrice = BigDecimal("50000.00"),
            quantity = 0.5,
            description = "Test order",
            createdAt = now.minusDays(1),
            lastStatusChange = null
        )

        createOrderInfo = CreateOrderInfo(
            walletId = testWallet.id!!,
            cardId = testCard.id!!,
            typeName = testOrderTypeBuy.name,
            statusName = "", // По умолчанию будет "модерация"
            unitPrice = BigDecimal("51000.00"),
            quantity = 0.2,
            description = "New test order"
        )
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    inner class CrudTests {

        @Test
        fun `findAll should return list from repository`() {
            val orders = listOf(testOrder, testOrder.copy(id = 501L))
            whenever(orderRepository.findAll()).thenReturn(orders)

            val result = orderService.findAll()

            assertEquals(orders, result)
            verify(orderRepository).findAll()
        }

        @Test
        fun `findById should return order when found and user exists`() {
            whenever(orderRepository.findById(testOrder.id!!)).thenReturn(Optional.of(testOrder))

            val result = orderService.findById(testOrder.id!!)

            assertEquals(testOrder, result)
            verify(orderRepository).findById(testOrder.id!!)
        }

        @Test
        fun `findById should throw NotFoundOrderException when order not found`() {
            val nonExistentId = 999L
            whenever(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty())

            val exception = assertThrows<NotFoundOrderException> {
                orderService.findById(nonExistentId)
            }

            assertEquals("Order with Id:$nonExistentId not found", exception.message)
            verify(orderRepository).findById(nonExistentId)
        }

        @Test
        fun `findById should throw NotFoundOrderException when order found but user is null`() {
            // Случай маловероятный с внешними ключами, но код его проверяет
            val orderWithNullUserWallet = testOrder.copy(wallet = testWallet.copy(user=null))
            whenever(orderRepository.findById(testOrder.id!!)).thenReturn(Optional.of(orderWithNullUserWallet))

            val exception = assertThrows<NotFoundOrderException> {
                orderService.findById(testOrder.id!!)
            }

            assertEquals("Order with Id:${testOrder.id!!} has no associated user", exception.message)
            verify(orderRepository).findById(testOrder.id!!)
        }


        @Test
        fun `save should assign new ID for new order and call repository save`() {
            val newOrder = testOrder.copy(id = 0L) // Новый ордер без ID
            val lastOrderId = 1000L
            val expectedNewId = 1001L

            // Arrange
            whenever(orderRepository.existsById(0L)).thenReturn(false) // Существование по ID=0
            whenever(orderRepository.findLatestOrder()).thenReturn(lastOrderId)
            // Мокаем save, чтобы он возвращал ордер с присвоенным ID
            whenever(orderRepository.save(orderCaptor.capture())).thenAnswer { invocation ->
                invocation.getArgument<Order>(0) // Возвращаем то, что передали
            }

            // Act
            val savedOrder = orderService.save(newOrder)

            // Assert
            verify(orderRepository).existsById(0L)
            verify(orderRepository).findLatestOrder()
            verify(orderRepository).save(any<Order>())

            val capturedOrder = orderCaptor.value
            assertEquals(expectedNewId, capturedOrder.id) // Проверяем, что ID был присвоен
            assertEquals(expectedNewId, savedOrder.id)
        }

        @Test
        fun `save should not assign new ID for existing order and call repository save`() {
            val existingOrder = testOrder // Ордер с существующим ID

            // Arrange
            whenever(orderRepository.existsById(existingOrder.id!!)).thenReturn(true)
            whenever(orderRepository.save(orderCaptor.capture())).thenReturn(existingOrder)

            // Act
            val savedOrder = orderService.save(existingOrder)

            // Assert
            verify(orderRepository).existsById(existingOrder.id!!)
            verify(orderRepository, never()).findLatestOrder() // Не должен вызываться для существующих
            verify(orderRepository).save(existingOrder) // Должен быть вызван с тем же ордером

            val capturedOrder = orderCaptor.value
            assertEquals(existingOrder.id, capturedOrder.id) // ID не должен был измениться
            assertEquals(existingOrder, savedOrder)
        }

        @Test
        fun `delete should call repository delete after finding order`() {
            // Arrange: Мокаем findById, чтобы он не выбрасывал исключение
            // Мы не можем мокать findById напрямую, так как он в том же классе.
            // Вместо этого мокаем вызов репозитория внутри findById.
            whenever(orderRepository.findById(testOrder.id!!)).thenReturn(Optional.of(testOrder))
            // Убедимся, что delete ничего не возвращает (void)
            doNothing().whenever(orderRepository).delete(any<Order>())


            // Act
            orderService.delete(testOrder.id!!)

            // Assert
            verify(orderRepository).findById(testOrder.id!!) // Убеждаемся, что findById вызвал репозиторий
            verify(orderRepository).delete(testOrder) // Убеждаемся, что delete был вызван с правильным ордером
        }

        @Test
        fun `delete should re-throw NotFoundOrderException if order not found`() {
            val nonExistentId = 999L
            whenever(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty())

            // Act & Assert
            assertThrows<NotFoundOrderException> {
                orderService.delete(nonExistentId)
            }

            verify(orderRepository).findById(nonExistentId)
        }
    }


    @Nested
    @DisplayName("addNewOrder Tests")
    inner class AddOrderTests {

        @BeforeEach
        fun setupAddOrderMocks() {
            // Мокаем зависимости, которые вызываются при успешном сценарии
            whenever(walletService.findById(createOrderInfo.walletId)).thenReturn(testWallet)
            whenever(cardService.findById(createOrderInfo.cardId)).thenReturn(testCard)
            whenever(orderTypeService.findById(createOrderInfo.typeName)).thenReturn(testOrderTypeBuy)
            // Мокаем получение статуса по умолчанию, т.к. statusName пустой
            whenever(orderStatusService.getDefaultStatus()).thenReturn(testStatusModeration)
            // Мокаем save (с захватом аргумента), чтобы он возвращал переданный ордер
            whenever(orderRepository.existsById(anyLong())).thenReturn(false) // Для нового ордера
            whenever(orderRepository.findLatestOrder()).thenReturn(500L) // Для присвоения ID
            whenever(orderRepository.save(orderCaptor.capture())).thenAnswer { inv -> inv.getArgument(0) }
        }

        @Test
        fun `addNewOrder should create and save order successfully`() {
            // Act
            val newOrder = orderService.addNewOrder(createOrderInfo, testUser)

            // Assert
            assertNotNull(newOrder)
            assertEquals(testWallet, newOrder.wallet)
            assertEquals(testCard, newOrder.card)
            assertEquals(testOrderTypeBuy, newOrder.type)
            assertEquals(testStatusModeration, newOrder.status) // Статус по умолчанию
            assertEquals(createOrderInfo.unitPrice, newOrder.unitPrice)
            assertEquals(createOrderInfo.quantity, newOrder.quantity)
            assertEquals(createOrderInfo.description, newOrder.description)
            assertNotNull(newOrder.id) // ID должен быть присвоен при сохранении

            // Verify dependency calls
            verify(walletService).findById(createOrderInfo.walletId)
            verify(cardService).findById(createOrderInfo.cardId)
            verify(orderTypeService).findById(createOrderInfo.typeName)
            verify(orderStatusService).getDefaultStatus() // Вызывается, если statusName пуст
            verify(orderStatusService, never()).findById(anyString()) // Не вызывается, если statusName пуст
            verify(orderRepository).save(any<Order>())

            // Verify captured order details
            val captured = orderCaptor.value
            assertEquals(testWallet, captured.wallet)
            // ... (проверки остальных полей захваченного ордера)
            assertEquals(501L, captured.id) // Проверка присвоенного ID
        }

        @Test
        fun `addNewOrder should use specific status if provided`() {
            val infoWithStatus = createOrderInfo.copy(statusName = testStatusAvailable.name)
            // Мокаем findById для статуса
            whenever(orderStatusService.findById(testStatusAvailable.name)).thenReturn(testStatusAvailable)

            // Act
            val newOrder = orderService.addNewOrder(infoWithStatus, testUser)

            // Assert
            assertEquals(testStatusAvailable, newOrder.status)
            verify(orderStatusService, never()).getDefaultStatus() // Не должен вызываться
            verify(orderStatusService).findById(testStatusAvailable.name) // Должен вызываться
            verify(orderRepository).save(any<Order>())
        }


        @Test
        fun `addNewOrder should throw IllegalArgumentException for invalid input data`() {
            val invalidInfoPrice = createOrderInfo.copy(unitPrice = BigDecimal.ZERO)
            val invalidInfoQty = createOrderInfo.copy(quantity = 0.0)
            val invalidInfoWallet = createOrderInfo.copy(walletId = 0L)
            val invalidInfoCard = createOrderInfo.copy(cardId = -1L)

            assertThrows<IllegalArgumentException> { orderService.addNewOrder(invalidInfoPrice, testUser) }
            assertThrows<IllegalArgumentException> { orderService.addNewOrder(invalidInfoQty, testUser) }
            assertThrows<IllegalArgumentException> { orderService.addNewOrder(invalidInfoWallet, testUser) }
            assertThrows<IllegalArgumentException> { orderService.addNewOrder(invalidInfoCard, testUser) }

            // Убедиться, что сервисы не вызывались
            verifyNoInteractions(walletService, cardService, orderTypeService, orderStatusService, orderRepository)
        }

        @Test
        fun `addNewOrder should throw OwnershipException if wallet does not belong to user`() {
            val otherUserWallet = testWallet.copy(user = User(id = 2L, login="other"))
            whenever(walletService.findById(createOrderInfo.walletId)).thenReturn(otherUserWallet)

            assertThrows<OwnershipException> {
                orderService.addNewOrder(createOrderInfo, testUser)
            }

            verify(walletService).findById(createOrderInfo.walletId)
            verifyNoMoreInteractions(walletService) // Убедимся, что дальнейшие проверки не идут
            verifyNoInteractions(cardService, orderTypeService, orderStatusService, orderRepository)
        }

        @Test
        fun `addNewOrder should throw OwnershipException if card does not belong to user`() {
            val otherUserCard = testCard.copy(user = User(id = 2L, login="other"))
            whenever(cardService.findById(createOrderInfo.cardId)).thenReturn(otherUserCard)
            // Wallet check проходит
            whenever(walletService.findById(createOrderInfo.walletId)).thenReturn(testWallet)


            assertThrows<OwnershipException> {
                orderService.addNewOrder(createOrderInfo, testUser)
            }

            verify(walletService).findById(createOrderInfo.walletId) // Проверка кошелька была
            verify(cardService).findById(createOrderInfo.cardId) // Проверка карты была
            verifyNoMoreInteractions(cardService)
            verifyNoInteractions(orderTypeService, orderStatusService, orderRepository)
        }

        // Добавьте тесты на случаи, когда findById у зависимостей выбрасывает NotFound...Exception
        @Test
        fun `addNewOrder should propagate NotFoundWalletException`() {
            val exception = NotFoundWalletException("Wallet not found")
            whenever(walletService.findById(createOrderInfo.walletId)).thenThrow(exception)

            val thrown = assertThrows<NotFoundWalletException> {
                orderService.addNewOrder(createOrderInfo, testUser)
            }
            assertEquals(exception, thrown)
            verify(walletService).findById(createOrderInfo.walletId)
            verifyNoInteractions(cardService, orderTypeService, orderStatusService, orderRepository)
        }

        // Аналогичные тесты для NotFoundCardException, NotFoundOrderTypeException и т.д.

    }

    @Nested
    @DisplayName("updateOrder Tests")
    inner class UpdateOrderTests {

        private lateinit var updatedInfo: CreateOrderInfo
        private lateinit var updatedWallet: Wallet
        private lateinit var updatedCard: Card
        private lateinit var updatedType: OrderType
        private lateinit var updatedStatus: OrderStatus


        @BeforeEach
        fun setupUpdate() {
            updatedWallet = testWallet.copy(id = 101L) // Новый кошелек для обновления
            updatedCard = testCard.copy(id = 201L)   // Новая карта
            updatedType = testOrderTypeSell          // Новый тип
            updatedStatus = testStatusAvailable      // Новый статус

            updatedInfo = CreateOrderInfo(
                walletId = updatedWallet.id!!,
                cardId = updatedCard.id!!,
                typeName = updatedType.name,
                statusName = updatedStatus.name, // Явно указываем статус
                unitPrice = BigDecimal("55000.00"),
                quantity = 0.1,
                description = "Updated order info"
            )

            // Мокаем findById для самого ордера (через репозиторий)
            whenever(orderRepository.findById(testOrder.id!!)).thenReturn(Optional.of(testOrder))
            // Мокаем findById для зависимостей
            whenever(walletService.findById(updatedInfo.walletId)).thenReturn(updatedWallet)
            whenever(cardService.findById(updatedInfo.cardId)).thenReturn(updatedCard)
            whenever(orderTypeService.findById(updatedInfo.typeName)).thenReturn(updatedType)
            whenever(orderStatusService.findById(updatedInfo.statusName)).thenReturn(updatedStatus)
            // Мокаем save
            whenever(orderRepository.existsById(testOrder.id!!)).thenReturn(true) // Ордер существует
            whenever(orderRepository.save(orderCaptor.capture())).thenAnswer { inv -> inv.getArgument(0) }
        }

        @Test
        fun `updateOrder should update fields and save successfully`() {
            // Act
            val updatedOrderResult = orderService.updateOrder(testOrder.id!!, updatedInfo)

            // Assert
            assertNotNull(updatedOrderResult)
            assertEquals(testOrder.id, updatedOrderResult.id) // ID не должен меняться

            // Проверяем обновленные поля
            assertEquals(updatedWallet, updatedOrderResult.wallet)
            assertEquals(updatedCard, updatedOrderResult.card)
            assertEquals(updatedType, updatedOrderResult.type)
            assertEquals(updatedStatus, updatedOrderResult.status)
            assertEquals(updatedInfo.unitPrice, updatedOrderResult.unitPrice)
            assertEquals(updatedInfo.quantity, updatedOrderResult.quantity)
            assertEquals(updatedInfo.description, updatedOrderResult.description)

            // Verify calls
            verify(orderRepository).findById(testOrder.id!!) // Поиск ордера
            verify(walletService).findById(updatedInfo.walletId)
            verify(cardService).findById(updatedInfo.cardId)
            verify(orderTypeService).findById(updatedInfo.typeName)
            verify(orderStatusService).findById(updatedInfo.statusName)
            verify(orderRepository).save(any<Order>()) // Сохранение

            // Проверяем захваченный ордер
            val captured = orderCaptor.value
            assertEquals(updatedWallet, captured.wallet)
            assertEquals(updatedCard, captured.card)
            // ... и т.д.
        }

        @Test
        fun `updateOrder should throw NotFoundOrderException if order to update not found`() {
            val nonExistentId = 999L
            whenever(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty())

            assertThrows<NotFoundOrderException> {
                orderService.updateOrder(nonExistentId, updatedInfo)
            }

            verify(orderRepository).findById(nonExistentId)
            verifyNoInteractions(walletService, cardService, orderTypeService, orderStatusService)
            verify(orderRepository, never()).save(any())
        }

        // Добавьте тесты на случаи, когда findById у зависимостей (walletService, cardService и т.д.)
        // при обновлении выбрасывает исключение NotFound...Exception
        @Test
        fun `updateOrder should propagate NotFoundWalletException from walletService`() {
            val exception = NotFoundWalletException("Wallet not found")
            whenever(walletService.findById(updatedInfo.walletId)).thenThrow(exception)

            val thrown = assertThrows<NotFoundWalletException> {
                orderService.updateOrder(testOrder.id!!, updatedInfo)
            }

            assertEquals(exception, thrown)
            verify(orderRepository).findById(testOrder.id!!)
            verify(walletService).findById(updatedInfo.walletId)
            verifyNoInteractions(cardService, orderTypeService, orderStatusService) // Остальные не должны вызываться
            verify(orderRepository, never()).save(any())
        }
        // ... аналогичные тесты для cardService, orderTypeService, orderStatusService
    }


    @Nested
    @DisplayName("Status Change Tests")
    inner class StatusChangeTests {

        // Общая функция для мокирования save при смене статуса
        private fun mockSaveForStatusChange() {
            whenever(orderRepository.existsById(anyLong())).thenReturn(true) // Ордер существует
            whenever(orderRepository.save(orderCaptor.capture())).thenAnswer { inv -> inv.getArgument(0) }
        }

        // Общая функция для проверки смены статуса
        private fun testStatusChange(
            initialStatus: OrderStatus,
            targetStatus: OrderStatus,
            action: (Order) -> Order
        ) {
            val orderWithInitialStatus = testOrder.copy(status = initialStatus)
            whenever(orderStatusService.findById(targetStatus.name)).thenReturn(targetStatus)
            mockSaveForStatusChange()

            val resultOrder = action(orderWithInitialStatus)

            assertEquals(targetStatus, resultOrder.status)
            assertNotNull(resultOrder.lastStatusChange) // Время должно обновиться
            assertTrue(resultOrder.lastStatusChange!!.isAfter(orderWithInitialStatus.createdAt))

            verify(orderStatusService).findById(targetStatus.name)
            verify(orderRepository).save(any<Order>())

            val captured = orderCaptor.value
            assertEquals(targetStatus, captured.status)
            assertNotNull(captured.lastStatusChange)
        }

        // Общая функция для проверки неверного начального статуса
        private fun testInvalidInitialStatus(
            initialStatus: OrderStatus,
            action: (Order) -> Order
        ) {
            val orderWithWrongStatus = testOrder.copy(status = initialStatus)

            val exception = assertThrows<IllegalActionOrderException> {
                action(orderWithWrongStatus)
            }
            assertTrue(exception.message!!.contains("Illegal status for action: ${initialStatus.name}"))

            verifyNoInteractions(orderStatusService) // findById для нового статуса не вызывается
            verify(orderRepository, never()).save(any()) // save не вызывается
        }


        @Test
        fun `acceptModerationOrder should change status to Available`() {
            testStatusChange(testStatusModeration, testStatusAvailable) { orderService.acceptModerationOrder(it) }
        }
        @Test
        fun `acceptModerationOrder should throw for wrong initial status`() {
            testInvalidInitialStatus(testStatusAvailable) { orderService.acceptModerationOrder(it) }
        }


        @Test
        fun `rejectModerationOrder should change status to ClosedProblem`() {
            testStatusChange(testStatusModeration, testStatusClosedProblem) { orderService.rejectModerationOrder(it) }
        }
        @Test
        fun `rejectModerationOrder should throw for wrong initial status`() {
            testInvalidInitialStatus(testStatusAvailable) { orderService.rejectModerationOrder(it) }
        }


        @Test
        fun `takeInDealOrder should change status to InDeal`() {
            testStatusChange(testStatusAvailable, testStatusInDeal) { orderService.takeInDealOrder(it) }
        }
        @Test
        fun `takeInDealOrder should throw for wrong initial status`() {
            testInvalidInitialStatus(testStatusModeration) { orderService.takeInDealOrder(it) }
        }


        @Test
        fun `closeSuccessOrder should change status to ClosedSuccess`() {
            testStatusChange(testStatusInDeal, testStatusClosedSuccess) { orderService.closeSuccessOrder(it) }
        }
        @Test
        fun `closeSuccessOrder should throw for wrong initial status`() {
            testInvalidInitialStatus(testStatusAvailable) { orderService.closeSuccessOrder(it) }
        }

        @Test
        fun `returnToPlatformOrder should change status to Available`() {
            testStatusChange(testStatusInDeal, testStatusAvailable) { orderService.returnToPlatformOrder(it) }
        }
        @Test
        fun `returnToPlatformOrder should throw for wrong initial status`() {
            testInvalidInitialStatus(testStatusAvailable) { orderService.returnToPlatformOrder(it) }
        }


        @Test
        fun `closeIrrelevantOrderInDeal should change status to ClosedIrrelevant`() {
            testStatusChange(testStatusInDeal, testStatusClosedIrrelevant) { orderService.closeIrrelevantOrderInDeal(it) }
        }
        @Test
        fun `closeIrrelevantOrderInDeal should throw for wrong initial status`() {
            testInvalidInitialStatus(testStatusAvailable) { orderService.closeIrrelevantOrderInDeal(it) }
        }


        @Test
        fun `closeIrrelevantOrder should change status to ClosedIrrelevant for valid statuses`() {
            val validInitialStatuses = listOf(testStatusModeration, testStatusAvailable) // Добавьте другие, если нужно
            whenever(orderStatusService.findById(testStatusClosedIrrelevant.name)).thenReturn(testStatusClosedIrrelevant)
            mockSaveForStatusChange()


            validInitialStatuses.forEach { initialStatus ->
                val order = testOrder.copy(status = initialStatus)
                val result = orderService.closeIrrelevantOrder(order)
                assertEquals(testStatusClosedIrrelevant, result.status)
                verify(orderRepository, atLeastOnce()).save(any<Order>()) // Проверяем, что save вызывался
            }
            verify(orderStatusService, times(validInitialStatuses.size)).findById(testStatusClosedIrrelevant.name)

        }

        @Test
        fun `closeIrrelevantOrder should throw for invalid statuses`() {
            val invalidInitialStatuses = listOf(testStatusInDeal, testStatusClosedSuccess)

            invalidInitialStatuses.forEach { initialStatus ->
                val order = testOrder.copy(status = initialStatus)
                val exception = assertThrows<IllegalActionOrderException> {
                    orderService.closeIrrelevantOrder(order)
                }
                assertTrue(exception.message!!.contains("Illegal status for action: ${initialStatus.name}"))
            }

            verifyNoInteractions(orderStatusService)
            verify(orderRepository, never()).save(any())
        }
    }

    @Nested
    @DisplayName("Helper and Info Methods")
    inner class HelperMethodsTests {

        @Test
        fun `isBuying should return true for Buy type`() {
            val buyOrder = testOrder.copy(type = testOrderTypeBuy)
            assertTrue(orderService.isBuying(buyOrder))
        }

        @Test
        fun `isBuying should return false for Sell type`() {
            val sellOrder = testOrder.copy(type = testOrderTypeSell)
            assertFalse(orderService.isBuying(sellOrder))
        }

        @Test
        fun `oppositeType should return Sell for Buy order`() {
            val buyOrder = testOrder.copy(type = testOrderTypeBuy)
            whenever(orderTypeService.findById("Продажа")).thenReturn(testOrderTypeSell)

            val opposite = orderService.oppositeType(buyOrder)

            assertEquals(testOrderTypeSell, opposite)
            verify(orderTypeService).findById("Продажа")
        }

        @Test
        fun `oppositeType should return Buy for Sell order`() {
            val sellOrder = testOrder.copy(type = testOrderTypeSell)
            whenever(orderTypeService.findById("Покупка")).thenReturn(testOrderTypeBuy)

            val opposite = orderService.oppositeType(sellOrder)

            assertEquals(testOrderTypeBuy, opposite)
            verify(orderTypeService).findById("Покупка")
        }

        @Test
        fun `orderToOrderInfo should map all fields correctly`() {
            val order = testOrder.copy(
                lastStatusChange = now // Установим время для проверки
            )

            val expectedInfo = OrderInfo(
                id = order.id,
                userLogin = order.wallet?.user?.login ?: "",
                walletId = order.wallet!!.id!!,
                cryptocurrencyCode = order.wallet?.cryptocurrency?.code ?: "",
                cardId = order.card!!.id!!,
                typeName = order.type!!.name,
                statusName = order.status!!.name,
                unitPrice = order.unitPrice,
                quantity = order.quantity,
                description = order.description,
                createdAt = order.createdAt.toString(),
                lastStatusChange = order.lastStatusChange.toString()
            )

            val actualInfo = orderService.orderToOrderInfo(order)

            assertEquals(expectedInfo, actualInfo)
        }

        @Test
        fun `orderToOrderInfo should use createdAt if lastStatusChange is null`() {
            val order = testOrder.copy(lastStatusChange = null)
            val expectedInfo = OrderInfo(
                id = order.id,
                userLogin = order.wallet?.user?.login?:"",
                walletId = order.wallet!!.id,
                cryptocurrencyCode = order.wallet?.cryptocurrency?.code ?: "",
                cardId = order.card!!.id,
                typeName = order.type!!.name,
                statusName = order.status!!.name,
                unitPrice = order.unitPrice,
                quantity = order.quantity,
                description = order.description,
                createdAt = order.createdAt.toString(),
                lastStatusChange = (order.lastStatusChange ?: order.createdAt).toString(),
            )
            val actualInfo = orderService.orderToOrderInfo(order)
            assertEquals(expectedInfo.lastStatusChange, actualInfo.lastStatusChange)
        }
    }

    @Nested
    @DisplayName("findByFilters Tests")
    inner class FilterTests {

        private val pageable: Pageable = PageRequest.of(0, 10)
        private val mockPage: Page<Order> = PageImpl(listOf(testOrder))

        @BeforeEach
        fun setupFilterMocks() {
            // Мокаем общий вызов findAll со спецификацией и пагинацией
            whenever(orderRepository.findAll(any<Specification<Order>>(), any<Pageable>())).thenReturn(mockPage)
        }

        @Test
        fun `findByFilters should call repository with null spec and default sort if no filters`() {
            val result = orderService.findByFilters(null, null, null, null, pageable)

            assertEquals(mockPage, result)
            verify(orderRepository).findAll(isNull<Specification<Order>>(), pageableCaptor.capture())
            assertEquals(Sort.by(Sort.Order.asc("createdAt")), pageableCaptor.value.sort)
            assertEquals(pageable.pageNumber, pageableCaptor.value.pageNumber)
            assertEquals(pageable.pageSize, pageableCaptor.value.pageSize)
        }

        @Test
        fun `findByFilters should call repository with correct spec and sort`() {
            val statusFilter = "модерация"
            val typeFilter = "Покупка"
            val cryptoFilter = "BTC"
            val dateFilter = now.minusDays(2).toString()
            val sortOrder = "desc"

            // Здесь мы не проверяем саму спецификацию детально, а лишь факт ее передачи
            val result = orderService.findByFilters(statusFilter, typeFilter, cryptoFilter, dateFilter, pageable, sortOrder)

            assertEquals(mockPage, result)
            // Захватываем аргументы для проверки
            verify(orderRepository).findAll(specificationCaptor.capture(), pageableCaptor.capture())

            // Проверяем, что спецификация не null (т.к. фильтры были)
            assertNotNull(specificationCaptor.value)

            // Проверяем Pageable (сортировка и пагинация)
            val capturedPageable = pageableCaptor.value
            assertEquals(Sort.by(Sort.Order.desc("createdAt")), capturedPageable.sort)
            assertEquals(pageable.pageNumber, capturedPageable.pageNumber)
            assertEquals(pageable.pageSize, capturedPageable.pageSize)
        }

        @Test
        fun `findByFilters should handle null date filter`() {
            orderService.findByFilters(null, null, null, null, pageable)
            verify(orderRepository).findAll(isNull<Specification<Order>>(), any<Pageable>())
        }

        @Test
        fun `findByFilters should throw IllegalArgumentException for invalid date format`() {
            val invalidDate = "not-a-date"
            assertThrows<IllegalArgumentException> {
                orderService.findByFilters(null, null, null, invalidDate, pageable)
            }
            verify(orderRepository, never()).findAll(any<Specification<Order>>(), any<Pageable>())
        }

        // Можно добавить тест на неизвестный фильтр, если OrderSpecifications может его выбросить,
        // но в текущей реализации buildSpecifications не имеет default ветки с выбросом исключения
        // для неизвестного ключа.
    }
}