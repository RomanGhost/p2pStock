package com.example.p2p_stock.services

import DealSpecifications
import com.example.p2p_stock.dataclasses.CreateDealInfo
import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.errors.DealUserSameException
import com.example.p2p_stock.errors.IllegalActionDealException
import com.example.p2p_stock.errors.NotFoundDealException
import com.example.p2p_stock.models.*
import com.example.p2p_stock.repositories.DealRepository
import com.example.p2p_stock.repositories.TaskRepository
import com.example.p2p_stock.services.kafka.deal_topics.KafkaProducer
import jakarta.security.auth.message.callback.GroupPrincipalCallback
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Service
class DealService(
    private val dealRepository: DealRepository,
    private val orderService: OrderService,
    private val walletService: WalletService,
    private val cardService: CardService,
    private val dealStatusService: DealStatusService,
    private val taskRepository: TaskRepository,
    private val priorityService: PriorityService,
    private val kafkaProducer: KafkaProducer
) {
    fun findAll(): List<Deal> = dealRepository.findAll()

    fun findByFilters(
        user: User,
        statusName: String?,
        changedAfter: String?,
        pageable: Pageable,
        sortOrder: String? = "asc"
    ): Page<Deal> {
        val changedAfterDate = parseDate(changedAfter)
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

    fun save(deal: Deal, force: Boolean=false): Deal {
        if (!dealRepository.existsById(deal.id) || deal.id == 0L){
            deal.id = getLastDealId() + 1
        }
        return dealRepository.save(deal)
    }
    private fun getLastDealId():Long = dealRepository.findLatestDeal()  ?: 0L

    fun addNewDeal(dealInfo: CreateDealInfo, user: User): Deal {
        var order = orderService.findById(dealInfo.counterpartyOrderId)
        validateCounterparty(order, user)

        val wallet = walletService.validateOwnership(dealInfo.walletId, user)
        val card = cardService.validateOwnership(dealInfo.cardId, user)

        val newOrder = createCounterpartyOrder(order, wallet, card)
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
        validateUserAccess(user, deal)
        return when(deal.status?.name){
            "Подтверждение сделки" -> updateStatus(deal, "Ожидание перевода")
            "Ожидание перевода" -> updateStatus(deal, "Ожидание подтверждения перевода")
            "Ожидание подтверждения перевода" -> {
                kafkaProducer.sendMessage(deal)

                updateStatus(deal, "Закрыто: успешно")
            }
            else -> deal
        }
    }

    fun negativeChangeStatus(user: User, deal: Deal): Deal {
        validateUserAccess(user, deal)
        return when(deal.status?.name){
            "Подтверждение сделки" -> denyDeal(deal)
            "Ожидание перевода", "Ожидание подтверждения перевода" -> callManager(deal)
            else -> deal
        }
    }

    fun managerTakeInWork(user: User, deal: Deal){
//        validateUserAccess(user, deal)
        if(deal.status?.name == "Приостановлено: решение проблем"){
            updateStatus(deal, "Ожидание решения менеджера")
        }
    }

    fun managerApprove(deal:Deal): Deal {
        if (deal.status?.name == "Ожидание решения менеджера") {
            kafkaProducer.sendMessage(deal)

            return updateStatus(deal, "Закрыто: успешно")
        }
        return deal
    }

    fun managerReject(deal:Deal): Deal {
        if (deal.status?.name == "Ожидание решения менеджера") {
            kafkaProducer.sendMessage(deal)

            return updateStatus(deal, "Закрыто: отменена менеджером")
        }
        return deal
    }

    private fun denyDeal(deal: Deal): Deal {

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

        kafkaProducer.sendMessage(deal)

        return updateStatus(deal, "Закрыто: неактуально")
    }

    private fun callManager(deal: Deal): Deal {
        val description = "Deal status description: ${deal.status?.name?:""}"

        val quantity = deal.buyOrder?.quantity?:0.0
        val pricePerUnit = deal.buyOrder?.unitPrice?:0

        val amount = pricePerUnit.toDouble() * quantity
        val priority = priorityService.findByAmount(amount)

        val task = Task(
            deal = deal,
            errorDescription = description,
            priority=priority,
        )

        taskRepository.save(task)

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
        card: Card
    ): Order {
        return orderService.takeInDealOrder(
            Order(
                wallet = wallet,
                card = card,
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
        if (order.wallet?.user?.id == user.id) throw DealUserSameException("The user cannot be their own counterparty")
    }

    private fun validateEarlyUserAccess(user: User, deal: Deal) {
        val earlyOrder = getEarlyOrder(deal)
        if (earlyOrder.wallet?.user?.id != user.id) throw IllegalActionDealException("User ${user.id} has no access to deal ${deal.id}")
    }

    private fun validateUserAccess(user: User, deal: Deal) {
        if(deal.buyOrder?.wallet?.user?.id != user.id && deal.sellOrder?.wallet?.user?.id != user.id)
            throw IllegalActionDealException("User ${user.id} has no access to deal ${deal.id}")
    }

    private fun validateOrderUser(order: Order?, user: User) {
        if (order?.wallet?.user?.id != user.id) throw IllegalActionDealException("User ${user.id} has no access to order ${order?.id}")
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
                    "lastStatusChange" -> DealSpecifications.lastStatusChangeAfter(value as LocalDate)
                    "userId" -> DealSpecifications.userDeals(value as Long)
                    else -> throw IllegalArgumentException("Unknown filter: $key")
                }
            }.reduceOrNull(Specification<Deal>::and)

    fun parseDate(dateString: String?): LocalDateTime? =
        try {
            dateString?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date format for deal 'changedAfter': $dateString")
        }
}
