package com.example.p2p_stock.controllers

import com.example.p2p_stock.dataclasses.DataInfo
import com.example.p2p_stock.services.*
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/data")
class DataController(
    private val bankService: BankService,
    private val cryptocurrencyService: CryptocurrencyService,
    private val dealStatusService: DealStatusService,
    private val orderTypeService: OrderTypeService,
    private val orderStatusService: OrderStatusService,
    private val priorityService: PriorityService,
    private val roleService: RoleService
) {

    // Универсальный метод для преобразования данных в DataResponse
    private fun <T> toDataResponse(items: List<T>, mapper: (T) -> DataInfo): List<DataInfo> {
        return items.map(mapper)
    }

    @GetMapping("/get/all/banks")
    fun getAllBanks(): List<DataInfo> {
        return toDataResponse(bankService.findAll()) { DataInfo(it.name) }
    }

    @GetMapping("/get/all/crypto")
    fun getAllCrypto(): List<DataInfo> {
        return toDataResponse(cryptocurrencyService.findAll()) { DataInfo(it.name, it.code) }
    }

    @GetMapping("/get/all/deal_statuses")
    fun getAllDealStatuses(): List<DataInfo> {
        return toDataResponse(dealStatusService.findAll()) { DataInfo(it.name) }
    }

    @GetMapping("/get/all/order_types")
    fun getAllOrderTypes(): List<DataInfo> {
        return toDataResponse(orderTypeService.findAll()) { DataInfo(it.name) }
    }

    @GetMapping("/get/all/order_statuses")
    fun getAllOrderStatuses(): List<DataInfo> {
        return toDataResponse(orderStatusService.findAll()) { DataInfo(it.name) }
    }

    @GetMapping("/get/all/priorities")
    fun getAllPriorities(): List<DataInfo> {
        return toDataResponse(priorityService.findAll()) { DataInfo(it.name) }
    }

    @GetMapping("/get/all/roles")
    fun getAllRoles(): List<DataInfo> {
        return toDataResponse(roleService.findAll()) { DataInfo(it.name) }
    }
}
