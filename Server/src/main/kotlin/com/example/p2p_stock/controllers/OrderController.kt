    package com.example.p2p_stock.controllers

    import OrderSpecifications
    import com.example.p2p_stock.dataclasses.CreateOrderInfo
    import com.example.p2p_stock.dataclasses.OrderInfo
    import com.example.p2p_stock.errors.UserException
    import com.example.p2p_stock.models.Order
    import com.example.p2p_stock.services.OrderService
    import com.example.p2p_stock.services.UserService
    import org.springframework.data.domain.Page
    import org.springframework.data.domain.Pageable
    import org.springframework.security.core.annotation.AuthenticationPrincipal
    import org.springframework.security.core.userdetails.UserDetails
    import org.springframework.web.bind.annotation.*
    import java.time.LocalDateTime
    import java.time.format.DateTimeParseException

    @CrossOrigin
    @RestController
    @RequestMapping("\${application.info.apiLink}/order")
    class OrderController(
        private val userService: UserService,
        private val orderService: OrderService,
    ) {
        // Получение всех заказов
        @GetMapping("/get_all")
        fun getAllOrders(): List<OrderInfo> {
            return orderService.findAll().map { mapToOrderInfo(it) }
        }

        // Получение заказа по ID
        @GetMapping("/get/{orderId}")
        fun getOrderById(@PathVariable orderId: Long): OrderInfo {
            val order = orderService.findById(orderId)
            return mapToOrderInfo(order)
        }

        @GetMapping("/get/filter")
        fun getFilteredOrders(
            @RequestParam(required = false) status: String?,
            @RequestParam(required = false) type: String?,
            @RequestParam(required = false) cryptoCode: String?,
            @RequestParam(required = false) createdAfter: String?,
            @RequestParam(required = false) sortOrder: String?,
            pageable: Pageable
        ): Page<OrderInfo> {
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
                "sortOrder" to sortOrder,
            )
            // Используем пагинацию и спецификации
            val orders = orderService.findByFilters(filters, pageable)
            return orders.map { mapToOrderInfo(it) }
        }



        // Добавление нового заказа
        @PostMapping("/add")
        fun addOrder(@AuthenticationPrincipal userDetails: UserDetails, @RequestBody newOrderInfo: CreateOrderInfo): OrderInfo {
            println(newOrderInfo)

            val username = userDetails.username

            // Получаем пользователя из базы данных по username
            val user = userService.findByEmail(username) ?: throw UserException("Пользователь не найден")

            val newOrder =  orderService.addNewOrder(newOrderInfo, user)
            return mapToOrderInfo(newOrder)
        }

        // Преобразование Order в OrderInfo
        private fun mapToOrderInfo(order: Order): OrderInfo {
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
