package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.RegisterUser
import com.example.p2p_stock.models.Role
import com.example.p2p_stock.models.User
import com.example.p2p_stock.repositories.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.domain.PageRequest

@SpringBootTest(properties = ["spring.profiles.active=test"])
@Transactional
class UserServiceTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var userService: UserService

    private lateinit var testUser: User
    private lateinit var testRole: Role
    private lateinit var registerUser: RegisterUser

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, roleService, passwordEncoder)

        testRole = roleService.save(Role("USER"))
        testUser = userRepository.save(
            User(
                login = "testuser",
                email = "test@example.com",
                password = passwordEncoder.encode("password123"),
                role = testRole,
                isActive = true
            )
        )

        registerUser = RegisterUser(
            login = "newuser",
            email = "new@example.com",
            password = "password123"
        )
    }

    @Test
    fun `should find all users`() {
        val result = userService.findAll()
        assertTrue(result.any { it.login == testUser.login })
    }

    @Test
    fun `should find user by id`() {
        val result = userService.findById(testUser.id)
        assertEquals(testUser.email, result!!.email)
    }

    @Test
    fun `should find user by login`() {
        val result = userService.findByUsername("testuser")
        assertEquals(testUser.email, result!!.email)
    }

    @Test
    fun `should find user by email`() {
        val result = userService.findByEmail("test@example.com")
        assertEquals(testUser.login, result!!.login)
    }

    @Test
    fun `should register new user`() {
        val result = userService.register(registerUser)
        assertEquals(registerUser.email, result.email)
    }

    @Test
    fun `should not register if email exists`() {
        val duplicate = RegisterUser("dupuser", "test@example.com", "123")
        assertThrows(IllegalArgumentException::class.java) {
            userService.register(duplicate)
        }
    }

    @Test
    fun `should not register if login exists`() {
        val duplicate = RegisterUser("testuser", "unique@example.com", "123")
        assertThrows(IllegalArgumentException::class.java) {
            userService.register(duplicate)
        }
    }

    @Test
    fun `should block user`() {
        val result = userService.blockUser(testUser)
        assertFalse(result.isActive)
    }

    @Test
    fun `should unblock user`() {
        testUser = userRepository.save(testUser.copy(isActive = false))
        val result = userService.unblockUser(testUser)
        assertTrue(result.isActive)
    }

    @Test
    fun `should convert to user info`() {
        val info = userService.userToUserInfo(testUser)
        assertEquals(testUser.login, info.login)
    }

    @Test
    fun `should convert to user for admin`() {
        val adminInfo = userService.userToUserForAdmin(testUser)
        assertEquals(testUser.email, adminInfo.email)
    }

    @Test
    fun `should delete user`() {
        userService.delete(testUser.id)
        val found = userRepository.findById(testUser.id)
        assertFalse(found.isPresent)
    }

    @Test
    fun `should find by filters`() {
        val pageable = PageRequest.of(0, 10)
        val page = userService.findByFilters(null, null, null, null, "admin@example.com", pageable)
        assertTrue(page.content.any { it.email == testUser.email })
    }

    @Test
    fun `should get user role`() {
        val role = userService.getUserRole(testUser.id)
        assertEquals(testRole.name, role?.name)
    }
}
