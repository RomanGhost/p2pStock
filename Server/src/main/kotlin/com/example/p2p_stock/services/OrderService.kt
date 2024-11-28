package com.example.p2p_stock.services

import OrderSpecifications
import com.example.p2p_stock.dataclasses.CreateOrderInfo
import com.example.p2p_stock.dataclasses.OrderInfo
import com.example.p2p_stock.errors.IllegalActionOrderException
import com.example.p2p_stock.errors.NotFoundOrderException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.Order
import com.example.p2p_stock.models.OrderType
import com.example.p2p_stock.models.User
import com.example.p2p_stock.repositories.OrderRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val walletService: WalletService,
    private val cardService: CardService,
    private val orderTypeService: OrderTypeService,
    private val orderStatusService: OrderStatusService,
) {
    private val logger: Logger = LoggerFactory.getLogger(OrderService::class.java)

    // Основные операции над заказами
    fun findAll(): List<Order> =
        orderRepository.findAll().filter { it.user != null }

    fun findById(orderId: Long): Order {
        val order = orderRepository.findById(orderId).orElseThrow {
            NotFoundOrderException("Order with Id:$orderId not found")
        }
        if (order.user == null) {
            throw NotFoundOrderException("Order with Id:$orderId has no associated user")
        }
        return order
    }

    fun save(order: Order): Order = orderRepository.save(order)

    fun delete(orderId: Long) {
        val order = findById(orderId)
        orderRepository.delete(order)
    }

    // Работа с фильтрами
    fun findByFilters(
        status: String?,
        type: String?,
        cryptoCode: String?,
        createdAfter: String?,
        pageable: Pageable,
        sortOrder: String? = "asc"
    ): Page<Order> {
        val createdAfterDate = parseDate(createdAfter, "Invalid date format for 'createdAfter': $createdAfter")

        val filters = mapOf(
            "status" to status,
            "type" to type,
            "cryptoCode" to cryptoCode,
            "createdAfter" to createdAfterDate?.toString()
        )

        val sort = Sort.by(if (sortOrder == "desc") Sort.Order.desc("createdAt") else Sort.Order.asc("createdAt"))
        val spec = buildSpecifications(filters) ?: Specification.where(null)
        val pageableWithSort = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)

        return orderRepository.findAll(spec, pageableWithSort)
    }

    // Создание и обновление заказов
    fun addNewOrder(orderInfo: CreateOrderInfo, user: User): Order {
        validateCreateOrderInfo(orderInfo)

        val wallet = validateWalletOwnership(orderInfo.walletId, user)
        val card = validateCardOwnership(orderInfo.cardId, user)
        val type = orderTypeService.findById(orderInfo.typeName)
        val status = resolveOrderStatus(orderInfo)

        val order = Order(
            user = user,
            wallet = wallet,
            card = card,
            type = type,
            status = status,
            unitPrice = orderInfo.unitPrice,
            quantity = orderInfo.quantity,
            description = orderInfo.description
        )

        return save(order)
    }

    fun updateOrder(orderId: Long, updatedInfo: CreateOrderInfo): Order {
        val order = findById(orderId)

        order.apply {
            wallet = walletService.findById(updatedInfo.walletId)
            card = cardService.findById(updatedInfo.cardId)
            type = orderTypeService.findById(updatedInfo.typeName)
            status = orderStatusService.findById(updatedInfo.statusName)
            unitPrice = updatedInfo.unitPrice
            quantity = updatedInfo.quantity
            description = updatedInfo.description
        }

        return save(order)
    }

    // Обновление статуса заказа
    private fun updateStatus(order: Order, newStatusName: String): Order {
        val newStatus = orderStatusService.findById(newStatusName)
        order.status = newStatus
        order.lastStatusChange = LocalDateTime.now()

        return save(order)
    }

    // Специальные операции с заказами
    fun acceptModerationOrder(order: Order): Order {
        validateStatus(order, "модерация")
        return updateStatus(order, "Доступна на платформе")
    }

    fun rejectModerationOrder(order: Order): Order {
        validateStatus(order, "модерация")
        return updateStatus(order, "Закрыто: проблема")
    }

    fun takeInDealOrder(order: Order): Order {
        validateStatus(order, "доступна на платформе")
        return updateStatus(order, "Используется в сделке")
    }

    fun closeSuccessOrder(order: Order): Order {
        validateStatus(order, "используется в сделке")
        return updateStatus(order, "Закрыто: успешно")
    }

    fun returnToPlatformOrder(order: Order): Order {
        validateStatus(order, "используется в сделке")
        return updateStatus(order, "Доступна на платформе")
    }

    fun closeIrrelevantOrderInDeal(order: Order): Order {
        validateStatus(order, "используется в сделке")
        return updateStatus(order, "Закрыто: неактуально")
    }

    fun closeIrrelevantOrder(order: Order): Order {
        val validStatuses = listOf("Модерация", "Отправлено на доработку", "Доступна на платформе")
        if (order.status!!.name in validStatuses) {
            return updateStatus(order, "Закрыто: неактуально")
        }
        throw IllegalActionOrderException("Illegal status for action: ${order.status!!.name}")
    }

    fun isBuying(order: Order): Boolean = order.type!!.name == "Покупка"

    fun oppositeType(order: Order): OrderType =
        if (isBuying(order)) orderTypeService.findById("Продажа") else orderTypeService.findById("Покупка")

    // Преобразование сущностей
    fun orderToOrderInfo(order: Order): OrderInfo = OrderInfo(
        id = order.id,
        userLogin = order.user!!.login,
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

    // Вспомогательные методы
    private fun validateCreateOrderInfo(orderInfo: CreateOrderInfo) {
        require(orderInfo.walletId > 0) { "Wallet ID must be greater than zero" }
        require(orderInfo.cardId > 0) { "Card ID must be greater than zero" }
        require(orderInfo.unitPrice > BigDecimal.ZERO) { "Unit price must be greater than zero" }
        require(orderInfo.quantity > 0) { "Quantity must be greater than zero" }
    }

    private fun validateStatus(order: Order, expectedStatus: String) {
        if (order.status!!.name.lowercase() != expectedStatus.lowercase()) {
            throw IllegalActionOrderException("Illegal status for action: ${order.status!!.name}")
        }
    }

    private fun validateWalletOwnership(walletId: Long, user: User) =
        walletService.findById(walletId).also {
            if (it.user?.id != user.id) throw OwnershipException("Wallet with id ${it.id} does not belong to user ${user.id}")
        }

    private fun validateCardOwnership(cardId: Long, user: User) =
        cardService.findById(cardId).also {
            if (it.user?.id != user.id) throw OwnershipException("Card with id ${it.id} does not belong to user ${user.id}")
        }

    private fun resolveOrderStatus(orderInfo: CreateOrderInfo) =
        if (orderInfo.statusName.isBlank()) orderStatusService.getDefaultStatus()
        else orderStatusService.findById(orderInfo.statusName)

    private fun buildSpecifications(filters: Map<String, String?>): Specification<Order>? =
        filters.entries.filter { it.value != null }.map { (key, value) ->
            when (key) {
                "status" -> OrderSpecifications.hasStatus(value!!)
                "type" -> OrderSpecifications.hasType(value!!)
                "cryptoCode" -> OrderSpecifications.hasCryptocurrencyCode(value!!)
                else -> throw IllegalArgumentException("Unknown filter parameter: $key")
            }
        }.reduceOrNull { spec1, spec2 -> spec1.and(spec2) }

    private fun parseDate(dateString: String?, errorMessage: String): LocalDateTime? =
        try {
            dateString?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(errorMessage)
        }
}
