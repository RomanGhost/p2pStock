package com.example.p2p_stock.services.sender

import com.example.p2p_stock.dataclasses.sender.KeyData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class ApiService(
    val webClient: WebClient,
    @Value("\${sender.host}") private val host: String,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {
    fun generateKeys(): KeyData {
        val url = "$host/keys/generate"
        var keyData: KeyData? = null

        while (keyData == null) {
            try {
                val response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String::class.java) // Получаем тело как строку
                    .block()

                println("Raw response: $response")

                // Преобразуем в KeyData, если тело не пустое
                if (!response.isNullOrBlank()) {
                    keyData = objectMapper.readValue(response, KeyData::class.java)
                } else {
                    println("Key data is null or empty, retrying...")
                    Thread.sleep(1000)
                }
            } catch (ex: WebClientResponseException) {
                println("HTTP Error: ${ex.statusCode} - ${ex.responseBodyAsString}")
                Thread.sleep(1000)
            } catch (ex: Exception) {
                println("Error: ${ex.message}")
                Thread.sleep(1000)
            }
        }
        return keyData
    }

}