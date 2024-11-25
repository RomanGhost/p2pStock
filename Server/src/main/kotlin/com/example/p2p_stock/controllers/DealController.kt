package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.CreateDealInfo
import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.errors.UserException
import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.services.DealService
import com.example.p2p_stock.services.UserService
import org.springframework.data.domain.Page
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
) {

    @GetMapping("/get/all")
    fun getAllDeals(): List<DealInfo> {
        val deals = dealService.findAll()
        return deals.map{ dealService.dealToDealInfo(it) }
    }

    @GetMapping("/get/{dealId}")
    fun getDealById(@PathVariable dealId: Long): DealInfo {
        val deal = dealService.findById(dealId)
        return dealService.dealToDealInfo(deal)
    }

    @GetMapping("/get/filter")
    fun getFilteredDeal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) statusName:String?,
        @RequestParam(required = false) changedAfter: String?,
        @RequestParam(required = false) sortOrder: String?,
        pageable: Pageable,
    ): PagedModel<DealInfo> {
        val username = userDetails.username

        // Получаем пользователя из базы данных по username
        val user = userService.findByEmail(username) ?: throw UserException("Пользователь не найден")

        val deal = dealService.findByFilters(user, statusName, changedAfter, pageable, sortOrder)

        return PagedModel(deal.map { dealService.dealToDealInfo(it) })
    }

    @PostMapping("/add")
    fun addDeal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody newDealInfo: CreateDealInfo,
    ): DealInfo {
        val username = userDetails.username

        // Получаем пользователя из базы данных по username
        val user = userService.findByEmail(username) ?: throw UserException("Пользователь не найден")

        val newDeal = dealService.addNewDeal(newDealInfo, user)
        return dealService.dealToDealInfo(newDeal)
    }

}