package com.example.p2p_stock.dataclasses

data class WalletInfo (
    val id:Long,
    val walletName:String,
    val balance: Double,
    val cryptocurrencyCode: String
)

