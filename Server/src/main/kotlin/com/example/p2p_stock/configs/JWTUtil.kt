package com.example.p2p_stock.configs

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey


@Service
class JWTUtil {
    private val secret: String = "testPhrasetestPhrasetestPhrasete"
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    private val expirationTime: Long = 24*60*60*1000 // 24 часа в миллисекундах

    fun generateToken(userDetails: MyUserDetails): String {
//        val userDetails = MyUserDetails(user)
        val claims: MutableMap<String, Any> = HashMap()
        claims["role"] = userDetails.authorities.firstOrNull() ?: SimpleGrantedAuthority("USER")
        return doGenerateToken(claims, userDetails.username)
    }

    fun doGenerateToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date())
            .expiration(Date((Date()).time + expirationTime))
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String, subject: String): Boolean {
        val claims = getAllClaimsFromToken(token)
        return (claims.subject == subject && !isTokenExpired(claims))
    }

    private fun getAllClaimsFromToken(token: String): Claims {
//        println("Token value: $token")
        return Jwts.parser() // Используем parserBuilder для создания парсера
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }


    fun getUsernameFromToken(token: String): String {
        return getClaimFromToken(token, Claims::getSubject)
    }

    fun getRoleUser(token:String): String {
        val roleMap = getAllClaimsFromToken(token)["role"] as Map<String, String>
        return roleMap["authority"]?:"USER"
    }


    fun <T> getClaimFromToken(token: String, claimsResolver: (Claims) -> T): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }
}
