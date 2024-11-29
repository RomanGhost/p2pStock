package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.CreateOrderInfo
import com.example.p2p_stock.dataclasses.OrderInfo
import com.example.p2p_stock.errors.UserException
import com.example.p2p_stock.services.OrderService
import com.example.p2p_stock.services.UserService
import com.example.p2p_stock.socket_handler.WebSocketHandler
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/order")
class OrderController(
    private val userService: UserService,
    private val orderService: OrderService,
    private val orderWebSocketHandler: WebSocketHandler<OrderInfo>
) {
    // Получение всех заказов
    @GetMapping("/get/all")
    fun getAllOrders(): List<OrderInfo> {
        return orderService.findAll().map { orderService.orderToOrderInfo(it) }
    }

    // Получение заказа по ID
    @GetMapping("/get/{orderId}")
    fun getOrderById(@PathVariable orderId: Long): OrderInfo {
        val order = orderService.findById(orderId)
        return orderService.orderToOrderInfo(order)
    }

    @GetMapping("/get/filter")
    fun getFilteredOrders(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) cryptoCode: String?,
        @RequestParam(required = false) createdAfter: String?,
        @RequestParam(required = false) sortOrder: String?,
        pageable: Pageable
    ): PagedModel<OrderInfo> {

        // Используем пагинацию и спецификации
        val orders =
            if(sortOrder == null) orderService.findByFilters(status, type, cryptoCode, createdAfter, pageable)
            else orderService.findByFilters(status, type, cryptoCode, createdAfter, pageable, sortOrder)

        return PagedModel(orders.map { orderService.orderToOrderInfo(it) })
    }

    // Добавление нового заказа
    @PostMapping("/add")
    fun addOrder(@AuthenticationPrincipal userDetails: UserDetails, @RequestBody newOrderInfo: CreateOrderInfo): OrderInfo {
        val username = userDetails.username

        // Получаем пользователя из базы данных по username
        val user = userService.findByEmail(username) ?: throw UserException("Пользователь не найден")

        val newOrder =  orderService.addNewOrder(newOrderInfo, user)
        val orderInfo = orderService.orderToOrderInfo(newOrder)

        orderWebSocketHandler.sendUpdateToAll(orderInfo)
        return orderInfo
    }

    @PatchMapping("/moderation/accept/{orderId}")
    fun acceptModeration(@PathVariable orderId: Long): OrderInfo {
        val order = orderService.findById(orderId)

        val updateOrder = orderService.acceptModerationOrder(order)
        val orderInfo = orderService.orderToOrderInfo(updateOrder)

        orderWebSocketHandler.sendUpdateToAll(orderInfo)
        return orderInfo
    }

    @PatchMapping("/moderation/reject/{orderId}")
    fun rejectModeration(@PathVariable orderId: Long): OrderInfo {
        val order = orderService.findById(orderId)

        val updateOrder = orderService.rejectModerationOrder(order)

        val orderInfo = orderService.orderToOrderInfo(updateOrder)
        orderWebSocketHandler.sendUpdateToAll(orderInfo)
        return orderInfo
    }

    @PatchMapping("/cancel/{orderId}")
    fun cancelModeration(@PathVariable orderId: Long): OrderInfo {
        val order = orderService.findById(orderId)

        val updateOrder = orderService.closeIrrelevantOrder(order)

        val orderInfo = orderService.orderToOrderInfo(updateOrder)
        orderWebSocketHandler.sendUpdateToAll(orderInfo)
        return orderInfo
    }
}
