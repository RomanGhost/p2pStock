package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Cryptocurrency
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CryptocurrencyRepository : JpaRepository<Cryptocurrency, String> {
    fun findByCode(code: String): Optional<Cryptocurrency>
}