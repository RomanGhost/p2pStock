package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.CreateDealInfo
import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.errors.DealUserSameException
import com.example.p2p_stock.errors.NotFoundDealException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.models.Order
import com.example.p2p_stock.models.User
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

    fun findAll(): List<Deal>{
        return dealRepository.findAll()
    }

    fun findByFilters(user:User, statusName:String?, changedAfter:String?, pageable: Pageable, sortOrder:String?="asc"): Page<Deal> {

        // Парсинг даты
        val changedAfterDate = try {
            changedAfter?.let { LocalDateTime.parse(it) }
        } catch (ex: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date format for 'createdAfter': $changedAfter")
        }

        // Построение спецификаций
        val filters = mutableMapOf(
            "statusName" to statusName,
            "lastStatusChange" to changedAfterDate,
            "userId" to user.id
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
        return dealRepository.findAll(spec, pageableWithSort)
    }


    fun findByBuyOrderId(buyOrderId: Long): Deal? = dealRepository.findByBuyOrderId(buyOrderId).orElse(null)
    fun findBySellOrderId(sellOrderId: Long): Deal? = dealRepository.findBySellOrderId(sellOrderId).orElse(null)

    fun findById(dealId:Long): Deal{
        val deal = dealRepository.findById(dealId).orElseThrow {
            NotFoundDealException("Order with Id:$dealId not found")
        }
        return deal
    }

    fun save(deal: Deal): Deal = dealRepository.save(deal)

    fun addNewDeal(dealInfo: CreateDealInfo, user: User): Deal {
        var order = orderService.findById(dealInfo.counterpartyOrderId)
        if(order.user?.id == user.id){
            throw DealUserSameException("The user cannot be his own counterparty")
        }

        val wallet = walletService.findById(dealInfo.walletId)
        if (wallet.user?.id != user.id) {
            throw OwnershipException("Wallet with id ${wallet.id} does not belong to user ${user.id}")
        }

        val card = cardService.findById(dealInfo.cardId)
        if (card.user?.id != user.id) {
            throw OwnershipException("Card with id ${card.id} does not belong to user ${user.id}")
        }

        // создание противоположной сделки
        var newOrder = Order(
            wallet = wallet,
            card = card,
            user = user,
            type = orderService.oppositeType(order),
            unitPrice = order.unitPrice,
            quantity = order.quantity,
            description = "Созданная сделка для контрагента",
        )

        // смена статуса
        order = orderService.updateStatus(order, "Используется в сделке")
        newOrder = orderService.updateStatus(newOrder, "Используется в сделке")

        var buyOrder = newOrder
        var sellOrder = order
        if(order.status!!.name == "Покупка"){
            buyOrder = order
            sellOrder = newOrder
        }

        val newDeal = Deal(
            buyOrder = buyOrder,
            sellOrder = sellOrder,
            status = dealStatusService.getFirstStatus(),
        )

        return dealRepository.save(newDeal)
    }

    fun delete(id: Long) = dealRepository.deleteById(id)

    fun dealToDealInfo(deal:Deal): DealInfo {
        val buyOrder = orderService.orderToOrderInfo(deal.buyOrder!!)
        val sellOrder = orderService.orderToOrderInfo(deal.sellOrder!!)

        return DealInfo(
            id = deal.id,
            buyOrder = buyOrder,
            sellOrder = sellOrder,
            statusName = deal.status!!.name,
            createdAt = deal.createdAt.toString(),
            lastStatusChange = deal.lastStatusChange.toString()
        )
    }

    private fun buildSpecifications(filters: Map<String, Any?>): Specification<Deal>? {
        val specifications = filters.entries
            .filter { it.value != null }
            .map { (key, value) ->
                when (key) {
                    "statusName" -> DealSpecifications.hasStatus(value!! as String)
                    "lastStatusChange" ->DealSpecifications.lastStatusChangeAfter(value!! as LocalDateTime)
                    "userId" ->DealSpecifications.userDeals(value!! as Long)
                    else -> throw IllegalArgumentException("Unknown filter parameter: $key")
                }
            }

        return specifications.reduceOrNull { spec1, spec2 -> spec1.and(spec2) }
    }
}

