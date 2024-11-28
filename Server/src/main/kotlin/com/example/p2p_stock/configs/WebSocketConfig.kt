package com.example.p2p_stock.configs

import com.example.p2p_stock.socket_handler.DealWebSocketHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@EnableWebSocket
@Configuration
class DealSocketConfig(
    @Value("\${application.info.websocket.link}") private val socketLink: String,
    private val dealWebSocketHandler: DealWebSocketHandler
):WebSocketConfigurer{
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(dealWebSocketHandler, "$socketLink/deals")
            .setAllowedOrigins("*")
    }

}