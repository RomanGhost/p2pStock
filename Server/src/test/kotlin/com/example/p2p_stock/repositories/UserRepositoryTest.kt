package com.example.p2p_stock.repositories

import com.example.p2p_stock.models.Role
import com.example.p2p_stock.models.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var roleRepository: RoleRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user1: User
    private lateinit var user2: User

    @BeforeEach
    fun setup() {
        val userRole = roleRepository.save(Role("USER"))
        val adminRole = roleRepository.save(Role("ADMIN"))
        user1 = userRepository.save(
            User(
                login = "john_doe",
                email = "john@example.com",
                password = "hashed_pass",
                role = userRole,
                isActive = true
            )
        )

        user2 = userRepository.save(
            User(
                login = "jane_doe",
                email = "jane@example.com",
                password = "hashed_pass2",
                role = adminRole,
                isActive = false
            )
        )
    }

    @Test
    fun `should find user by login`() {
        val found = userRepository.findByLogin("john_doe")
        assertTrue(found.isPresent)
        assertEquals(user1.email, found.get().email)
    }

    @Test
    fun `should find user by email`() {
        val found = userRepository.findByEmail("jane@example.com")
        assertTrue(found.isPresent)
        assertEquals(user2.login, found.get().login)
    }

    @Test
    fun `should check existence by login`() {
        assertTrue(userRepository.existsByLogin("john_doe"))
        assertFalse(userRepository.existsByLogin("not_existing"))
    }

    @Test
    fun `should check existence by email`() {
        assertTrue(userRepository.existsByEmail("jane@example.com"))
        assertFalse(userRepository.existsByEmail("nope@nowhere.com"))
    }

    @Test
    fun `should filter users with findByFilters`() {
        val pageable = PageRequest.of(0, 10)

        val filteredByActive = userRepository.findByFilters(
            id = null,
            isActive = true,
            adminEmail = "admin@example.com",
            pageable = pageable
        )

        assertEquals(1, filteredByActive.totalElements)
        assertEquals("john@example.com", filteredByActive.content[0].email)

        val filteredById = userRepository.findByFilters(
            id = user2.id,
            isActive = null,
            adminEmail = "admin@example.com",
            pageable = pageable
        )

        assertEquals(1, filteredById.totalElements)
        assertEquals("jane@example.com", filteredById.content[0].email)

        val excludingAdmin = userRepository.findByFilters(
            id = null,
            isActive = null,
            adminEmail = "jane@example.com", // exclude this one
            pageable = pageable
        )

        assertEquals(1, excludingAdmin.totalElements)
        assertEquals("john@example.com", excludingAdmin.content[0].email)
    }
}
