package com.example.p2p_stock.services

import DealSpecifications
import com.example.p2p_stock.dataclasses.CreateDealInfo
import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.errors.DealUserSameException
import com.example.p2p_stock.errors.IllegalActionDealException
import com.example.p2p_stock.errors.NotFoundDealException
import com.example.p2p_stock.models.*
import com.example.p2p_stock.repositories.DealRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Service
class DealService(
    private val dealRepository: DealRepository,
    private val orderService: OrderService,
    private val walletService: WalletService,
    private val cardService: CardService,
    private val dealStatusService: DealStatusService,
) {
    fun findAll(): List<Deal> = dealRepository.findAll()

    fun findByFilters(
        user: User,
        statusName: String?,
        changedAfter: String?,
        pageable: Pageable,
        sortOrder: String? = "asc"
    ): Page<Deal> {
        val changedAfterDate = parseDate(changedAfter, "Invalid date format for 'changedAfter': $changedAfter")
        val filters = mapOf(
            "statusName" to statusName,
            "lastStatusChange" to changedAfterDate,
            "userId" to user.id
        )
        val sort = Sort.by(
            if (sortOrder == "desc") Sort.Order.desc("createdAt") else Sort.Order.asc("createdAt")
        )
        val pageableWithSort = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)
        val spec = buildSpecifications(filters)
        return dealRepository.findAll(spec, pageableWithSort)
    }

    fun findById(dealId: Long): Deal =
        dealRepository.findById(dealId).orElseThrow {
            NotFoundDealException("Deal with ID $dealId not found")
        }

    fun findByBuyOrderId(buyOrderId: Long): Deal? =
        dealRepository.findByBuyOrderId(buyOrderId).orElse(null)

    fun findBySellOrderId(sellOrderId: Long): Deal? =
        dealRepository.findBySellOrderId(sellOrderId).orElse(null)

    fun save(deal: Deal): Deal = dealRepository.save(deal)

    fun addNewDeal(dealInfo: CreateDealInfo, user: User): Deal {
        var order = orderService.findById(dealInfo.counterpartyOrderId)
        validateCounterparty(order, user)

        val wallet = walletService.validateOwnership(dealInfo.walletId, user)
        val card = cardService.validateOwnership(dealInfo.cardId, user)

        val newOrder = createCounterpartyOrder(order, wallet, card, user)
        order = orderService.takeInDealOrder(order)

        val (buyOrder, sellOrder) = resolveOrderRoles(order, newOrder)

        val newDeal = Deal(
            buyOrder = buyOrder,
            sellOrder = sellOrder,
            status = dealStatusService.getFirstStatus()
        )

        return save(newDeal)
    }

    fun positiveChangeStatus(user: User, deal: Deal): Deal {
        return when(deal.status?.name){
            "Подтверждение сделки" -> confirmDeal(user, deal)
            "Ожидание перевода" -> confirmSendPayment(user, deal)
            "Ожидание подтверждения перевода" -> confirmGetPayment(user, deal)
            else -> deal
        }
    }

    fun negativeChangeStatus(user: User, deal: Deal): Deal {
        return when(deal.status?.name){
            "Подтверждение сделки" -> denyDeal(user, deal)
            "Ожидание перевода", "Ожидание подтверждения перевода" -> callManager(user, deal)
            else -> deal
        }
    }

    private fun confirmDeal(user: User, deal: Deal): Deal {
        validateEarlyUserAccess(user, deal)
//        validateDealStatus(deal, "Подтверждение сделки")
        return updateStatus(deal, "Ожидание перевода")
    }

    private fun denyDeal(user: User, deal: Deal): Deal {
        validateUserAccess(user, deal)
//        validateDealStatus(deal, "Подтверждение сделки")

        val early = orderService.returnToPlatformOrder(getEarlyOrder(deal))
        val older = orderService.closeIrrelevantOrderInDeal(getOlderOrder(deal))
        if(orderService.isBuying(early)){
            deal.buyOrder = early
            deal.sellOrder = older
        } else{
            deal.buyOrder = older
            deal.sellOrder = early
        }

        return updateStatus(deal, "Закрыто: неактуально")
    }

    private fun confirmSendPayment(user: User, deal: Deal): Deal {
        validateOrderUser(deal.buyOrder, user)
//        validateDealStatus(deal, "Ожидание перевода")
        return updateStatus(deal, "Ожидание подтверждения перевода")
    }

    private fun confirmGetPayment(user: User, deal: Deal): Deal {
        validateOrderUser(deal.sellOrder, user)
//        validateDealStatus(deal, "Ожидание подтверждения перевода")
        return updateStatus(deal, "Закрыто: успешно")
    }

    private fun callManager(user: User, deal: Deal): Deal {
        validateEarlyUserAccess(user, deal)
//        val accessStatuses = listOf("Ожидание перевода", "Ожидание подтверждения перевода")
//        for (statusName in accessStatuses){
//            validateDealStatus(deal, statusName)
//        }

        return updateStatus(deal, "Приостановлено: решение проблем")
    }

    fun delete(id: Long) = dealRepository.deleteById(id)

    fun dealToDealInfo(deal: Deal): DealInfo =
        DealInfo(
            id = deal.id,
            buyOrder = orderService.orderToOrderInfo(deal.buyOrder!!),
            sellOrder = orderService.orderToOrderInfo(deal.sellOrder!!),
            statusName = deal.status!!.name,
            createdAt = deal.createdAt.toString(),
            lastStatusChange = deal.lastStatusChange.toString()
        )

    private fun updateStatus(deal: Deal, newStatusName: String): Deal {
        deal.status = dealStatusService.findById(newStatusName)
        deal.lastStatusChange = LocalDateTime.now()
        return save(deal)
    }

    private fun createCounterpartyOrder(
        order: Order,
        wallet: Wallet,
        card: Card,
        user: User
    ): Order {
        return orderService.takeInDealOrder(
            Order(
                wallet = wallet,
                card = card,
                user = user,
                type = orderService.oppositeType(order),
                status = order.status,
                unitPrice = order.unitPrice,
                quantity = order.quantity,
                description = "Counterparty order for deal ${order.id}"
            )
        )
    }

    private fun resolveOrderRoles(order1: Order, order2: Order): Pair<Order, Order> =
        if (orderService.isBuying(order1)) order1 to order2 else order2 to order1

    private fun validateCounterparty(order: Order, user: User) {
        if (order.user?.id == user.id) throw DealUserSameException("The user cannot be their own counterparty")
    }

    private fun validateEarlyUserAccess(user: User, deal: Deal) {
        val earlyOrder = getEarlyOrder(deal)
        if (earlyOrder.user?.id != user.id) throw IllegalActionDealException("User ${user.id} has no access to deal ${deal.id}")
    }

    private fun validateUserAccess(user: User, deal: Deal) {
        if(deal.buyOrder?.user?.id != user.id && deal.sellOrder?.user?.id != user.id) throw IllegalActionDealException("User ${user.id} has no access to deal ${deal.id}")
    }

    private fun validateOrderUser(order: Order?, user: User) {
        if (order?.user?.id != user.id) throw IllegalActionDealException("User ${user.id} has no access to order ${order?.id}")
    }

    private fun validateDealStatus(deal: Deal, expectedStatus: String) {
        if (deal.status?.name != expectedStatus) {
            throw IllegalActionDealException("Deal ${deal.id} must have status '$expectedStatus'")
        }
    }

    fun getEarlyOrder(deal: Deal): Order =
        if (deal.buyOrder!!.createdAt > deal.sellOrder!!.createdAt) deal.sellOrder!! else deal.buyOrder!!

    fun getOlderOrder(deal: Deal): Order =
        if (deal.buyOrder!!.createdAt < deal.sellOrder!!.createdAt) deal.sellOrder!! else deal.buyOrder!!

    private fun buildSpecifications(filters: Map<String, Any?>): Specification<Deal>? =
        filters.filterValues { it != null }
            .map { (key, value) ->
                when (key) {
                    "statusName" -> DealSpecifications.hasStatus(value as String)
                    "lastStatusChange" -> DealSpecifications.lastStatusChangeAfter(value as LocalDateTime)
                    "userId" -> DealSpecifications.userDeals(value as Long)
                    else -> throw IllegalArgumentException("Unknown filter: $key")
                }
            }.reduceOrNull(Specification<Deal>::and)

    private fun parseDate(dateString: String?, errorMessage: String): LocalDateTime? =
        try {
            dateString?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(errorMessage)
        }
}
