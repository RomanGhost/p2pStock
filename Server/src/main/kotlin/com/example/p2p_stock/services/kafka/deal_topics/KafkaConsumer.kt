package com.example.p2p_stock.services.kafka.deal_topics


import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.dataclasses.kafka.DealKafkaInfo
import com.example.p2p_stock.dataclasses.kafka.OrderKafkaInfo
import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.models.Order
import com.example.p2p_stock.models.Wallet
import com.example.p2p_stock.services.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val cryptocurrencyService: CryptocurrencyService,
    private val dealService: DealService,
    private val dealStatusService: DealStatusService,
    private val orderTypeService: OrderTypeService,
    private val orderService: OrderService,
    private val orderStatusService: OrderStatusService,
    private val walletService: WalletService,

    private val objectMapper: ObjectMapper = ObjectMapper()
){

    private fun orderFromOrderKafkaInfo(orderKafkaInfo:OrderKafkaInfo): Order {
        var wallet = walletService.findByPublicKey(orderKafkaInfo.userHashPublicKey)
        if (wallet == null){
            val crypto = cryptocurrencyService.findByCode(orderKafkaInfo.cryptocurrencyCode)

            wallet = Wallet(publicKey = orderKafkaInfo.userHashPublicKey, cryptocurrency = crypto)
            walletService.save(wallet)
        }
        val orderType = orderTypeService.findById(orderKafkaInfo.typeName)
        val orderStatus = orderStatusService.findById("используется в сделке")

        val order = Order(
            id = orderKafkaInfo.id,
            wallet = wallet,
            type = orderType,
            status = orderStatus,
            unitPrice = orderKafkaInfo.unitPrice,
            quantity = orderKafkaInfo.quantity,
            createdAt = orderService.parseDate(orderKafkaInfo.createdAt)!!,
            lastStatusChange = orderService.parseDate(orderKafkaInfo.lastStatusChange)!!
        )
        return order
    }

    @KafkaListener(topics = ["\${spring.kafka.consumer.topic-name}",], groupId = "\${spring.kafka.consumer.group-id}")
    fun listen(message: String) {
        val dealInfo = objectMapper.readValue(message, DealKafkaInfo::class.java)
        var sellOrder = orderFromOrderKafkaInfo(dealInfo.sellOrder)
        var buyOrder = orderFromOrderKafkaInfo(dealInfo.buyOrder)

        sellOrder = orderService.save(sellOrder)
        buyOrder = orderService.save(buyOrder)

        val dealStatus = dealStatusService.findById(dealInfo.statusName)
        val deal = Deal(
            id = dealInfo.id,
            buyOrder=buyOrder,
            sellOrder = sellOrder,
            status = dealStatus,
            createdAt = dealService.parseDate(dealInfo.createdAt)!!,
            lastStatusChange = dealService.parseDate(dealInfo.lastStatusChange)!!,
        )

        dealService.save(deal)

        println("Received message: $message")
    }
}
