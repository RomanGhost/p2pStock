package com.example.p2p_stock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class P2pStockApplication

fun main(args: Array<String>) {
	runApplication<P2pStockApplication>(*args)
}
