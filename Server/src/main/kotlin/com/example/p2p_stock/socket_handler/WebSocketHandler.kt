package com.example.p2p_stock.socket_handler

import com.example.p2p_stock.dataclasses.DealInfo
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class DealWebSocketHandler() : TextWebSocketHandler() {
    private val objectMapper = ObjectMapper()
    private val sessions = mutableListOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
    }

    fun sendUpdateToAll(deal: DealInfo) {
        val message = objectMapper.writeValueAsString(deal)
        sessions.forEach { it.sendMessage(TextMessage(message)) }
    }
}
