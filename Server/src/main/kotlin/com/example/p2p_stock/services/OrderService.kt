package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.CreateOrderInfo
import com.example.p2p_stock.dataclasses.OrderInfo
import com.example.p2p_stock.errors.NotFoundOrderException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.Order
import com.example.p2p_stock.models.OrderType
import com.example.p2p_stock.models.User
import com.example.p2p_stock.repositories.OrderRepository
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

    fun findByFilters(status:String?, type:String?, cryptoCode:String?, createdAfter:String?, pageable: Pageable, sortOrder:String?="asc"): Page<Order> {
        // Парсинг даты
        val createdAfterDate = try {
            createdAfter?.let { LocalDateTime.parse(it) }
        } catch (ex: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date format for 'createdAfter': $createdAfter")
        }

        // Построение спецификаций
        val filters = mutableMapOf(
            "status" to status,
            "type" to type,
            "cryptoCode" to cryptoCode,
            "createdAfter" to createdAfterDate?.toString(),
        )

        // Сортировка по дате
        val sort = if (sortOrder == "desc") {
            Sort.by(Sort.Order.desc("createdAt"))  // Сортировка по дате в убывающем порядке
        } else {
            Sort.by(Sort.Order.asc("createdAt"))   // Сортировка по дате в возрастающем порядке
        }

        // Построение спецификаций
        val spec = buildSpecifications(filters) ?: Specification.where(null)


        // Создаем новый pageable с сортировкой
        val pageableWithSort = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)

        // Пагинированный запрос с сортировкой
        return orderRepository.findAll(spec, pageableWithSort)
    }

    fun save(order: Order): Order = orderRepository.save(order)

    fun delete(orderId: Long) {
        val order = findById(orderId)
        orderRepository.delete(order)
    }

    fun addNewOrder(orderInfo: CreateOrderInfo, user: User): Order {
        validateCreateOrderInfo(orderInfo)

        val wallet = walletService.findById(orderInfo.walletId)
        if (wallet.user?.id != user.id) {
            throw OwnershipException("Wallet with id ${wallet.id} does not belong to user ${user.id}")
        }

        val card = cardService.findById(orderInfo.cardId)
        if (card.user?.id != user.id) {
            throw OwnershipException("Card with id ${card.id} does not belong to user ${user.id}")
        }

        val type = orderTypeService.findById(orderInfo.typeName)
        val status = if (orderInfo.statusName.isBlank()) {
            orderStatusService.getDefaultStatus()
        } else {
            orderStatusService.findById(orderInfo.statusName)
        }

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

    fun updateStatus(order:Order, newStatusName:String): Order {
        val newStatus = orderStatusService.findById(newStatusName)
        order.status = newStatus
        order.lastStatusChange = LocalDateTime.now()

        return save(order)
    }

    fun oppositeType(order: Order): OrderType {
        val type = if (order.status!!.name == "Покупка"){
            orderTypeService.findById("Продажа")
        } else{
            orderTypeService.findById("Покупка")
        }

        return type
    }

    private fun buildSpecifications(filters: Map<String, String?>): Specification<Order>? {
        val specifications = filters.entries
            .filter { it.value != null }
            .map { (key, value) ->
                when (key) {
                    "status" -> OrderSpecifications.hasStatus(value!!)
                    "type" -> OrderSpecifications.hasType(value!!)
                    "cryptoCode" -> OrderSpecifications.hasCryptocurrencyCode(value!!)
                    else -> throw IllegalArgumentException("Unknown filter parameter: $key")
                }
            }

        return specifications.reduceOrNull { spec1, spec2 -> spec1.and(spec2) }
    }

    private fun validateCreateOrderInfo(orderInfo: CreateOrderInfo) {
        require(orderInfo.walletId > 0) { "Wallet ID must be greater than zero" }
        require(orderInfo.cardId > 0) { "Card ID must be greater than zero" }
        require(orderInfo.unitPrice > BigDecimal.ZERO) { "Unit price must be greater than zero" }
        require(orderInfo.quantity > 0) { "Quantity must be greater than zero" }
    }

    fun orderToOrderInfo(order: Order): OrderInfo {
        return OrderInfo(
            id = order.id,
            userLogin = order.user!!.login,
            walletId = order.wallet!!.id,
            cryptocurrencyCode = order.wallet?.cryptocurrency?.code?:"",
            cardId = order.card!!.id,
            typeName = order.type!!.name,
            statusName = order.status!!.name,
            unitPrice = order.unitPrice,
            quantity = order.quantity,
            description = order.description,
            createdAt = order.createdAt.toString(),
            lastStatusChange = (order.lastStatusChange ?: order.createdAt).toString(),
        )
    }
}
