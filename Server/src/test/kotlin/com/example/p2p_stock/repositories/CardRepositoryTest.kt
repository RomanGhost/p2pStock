package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class CardRepositoryTest {

    @Autowired private lateinit var cardRepository: CardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var bankRepository: BankRepository

    private lateinit var user: User
    private lateinit var bank: Bank

    @BeforeEach
    fun setup() {
        val role = roleRepository.save(Role("USER"))
        user = userRepository.save(
            User(login = "test_user", email = "user@example.com", password = "password", isActive = true, role = role)
        )

        bank = bankRepository.save(Bank(name = "TestBank"))
    }

    @Test
    fun `should save and find cards by user id`() {
        val card1 = cardRepository.save(
            Card(
                cardName = "Main Card",
                cardNumber = "1111222233334444",
                bank = bank,
                user = user
            )
        )

        val card2 = cardRepository.save(
            Card(
                cardName = "Backup Card",
                cardNumber = "5555666677778888",
                bank = bank,
                user = user
            )
        )

        val cards = cardRepository.findByUserId(user.id!!)
        assertEquals(2, cards.size)
        assertTrue(cards.any { it.cardNumber == "1111222233334444" })
        assertTrue(cards.all { it.user?.id == user.id })
    }

    @Test
    fun `should return empty list when user has no cards`() {
        val anotherUser = userRepository.save(
            User(login = "no_cards", email = "nocards@example.com", password = "1234", isActive = true, role = roleRepository.findAll().first())
        )

        val cards = cardRepository.findByUserId(anotherUser.id)
        assertTrue(cards.isEmpty())
    }
}
