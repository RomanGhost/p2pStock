package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.CreateDealInfo
import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.dataclasses.OrderInfo
import com.example.p2p_stock.errors.UserException
import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.models.User
import com.example.p2p_stock.services.DealService
import com.example.p2p_stock.services.UserService
import com.example.p2p_stock.socket_handler.WebSocketHandler
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/deal")
class DealController(
    private val userService: UserService,
    private val dealService: DealService,
    private val dealWebSocketHandler: WebSocketHandler<DealInfo>,
    private val orderWebSocketHandler: WebSocketHandler<OrderInfo>,
) {
    private fun getAuthenticatedUser(userDetails: UserDetails): User {
        return userService.findByEmail(userDetails.username) ?: throw UserException("Пользователь не найден")
    }

    private fun handleDealUpdate(
        userDetails: UserDetails,
        dealId: Long,
        updateFunction: (User, Deal) -> Deal
    ): DealInfo {
        val user = getAuthenticatedUser(userDetails)
        val deal = dealService.findById(dealId)
        val dealInfo = dealService.dealToDealInfo(updateFunction(user, deal))

        orderWebSocketHandler.sendUpdateToAll(dealInfo.sellOrder)
        orderWebSocketHandler.sendUpdateToAll(dealInfo.buyOrder)

        dealWebSocketHandler.sendUpdateToAll(dealInfo)
        return dealInfo
    }

    @GetMapping("/get/all")
    fun getAllDeals(): List<DealInfo> = dealService.findAll().map { dealService.dealToDealInfo(it) }

    @GetMapping("/get/{dealId}")
    fun getDealById(@PathVariable dealId: Long): DealInfo =
        dealService.dealToDealInfo(dealService.findById(dealId))

    @GetMapping("/get/filter")
    fun getFilteredDeal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) statusName: String?,
        @RequestParam(required = false) changedAfter: String?,
        @RequestParam(required = false) sortOrder: String?,
        pageable: Pageable
    ): PagedModel<DealInfo> {
        val user = getAuthenticatedUser(userDetails)
        val pages = dealService.findByFilters(user, statusName, changedAfter, pageable, sortOrder)
            .map { dealService.dealToDealInfo(it) }
        return PagedModel(pages)
    }

    @PostMapping("/add")
    fun addDeal(@AuthenticationPrincipal userDetails: UserDetails, @RequestBody newDealInfo: CreateDealInfo): DealInfo {
        val user = getAuthenticatedUser(userDetails)

        val newDeal = dealService.addNewDeal(newDealInfo, user)
        val earlyOrder = dealService.getEarlyOrder(newDeal)
        val dealInfo = dealService.dealToDealInfo(newDeal)

        val orderNotification = if(dealInfo.sellOrder.id == earlyOrder.id) dealInfo.sellOrder else dealInfo.buyOrder
        orderWebSocketHandler.sendUpdateToAll(orderNotification)

        dealWebSocketHandler.sendUpdateToAll(dealInfo)
        return dealInfo
    }

    @PatchMapping("status/confirm/next/{dealId}")
    fun nextConfirm(@PathVariable dealId: Long, @AuthenticationPrincipal userDetails: UserDetails): DealInfo {
        return handleDealUpdate(userDetails, dealId, dealService::positiveChangeStatus)
    }

    @PatchMapping("status/reject/next/{dealId}")
    fun nextReject(@PathVariable dealId: Long, @AuthenticationPrincipal userDetails: UserDetails): DealInfo {
        return handleDealUpdate(userDetails, dealId, dealService::negativeChangeStatus)
    }
}
