package com.example.p2p_stock.configs

import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.dataclasses.OrderInfo
import com.example.p2p_stock.socket_handler.WebSocketHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@CrossOrigin
@EnableWebSocket
@Configuration
class WebSocketConfig(
    @Value("\${application.info.websocket.link}") private val socketLink: String,
    private val dealWebSocketHandler: WebSocketHandler<DealInfo>,
    private val orderWebSocketHandler: WebSocketHandler<OrderInfo>,
):WebSocketConfigurer{
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(dealWebSocketHandler, "$socketLink/deal")
            .addHandler(orderWebSocketHandler, "$socketLink/order")
            .setAllowedOrigins("http://localhost:4200")
    }

}