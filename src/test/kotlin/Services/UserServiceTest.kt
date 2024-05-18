package com.example.p2p_project.services.Services

import com.example.p2p_project.models.User
import com.example.p2p_project.models.dataTables.Role
import com.example.p2p_project.repositories.UserRepository
import com.example.p2p_project.repositories.adjacent.UserRoleRepository
import com.example.p2p_project.services.UserService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userRoleRepository: UserRoleRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userRepository = mock(UserRepository::class.java)
        userRoleRepository = mock(UserRoleRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        userService = UserService(userRepository, userRoleRepository, passwordEncoder)
    }

    @Test
    fun `should return all users`() {
        val users = listOf(User(), User())
        `when`(userRepository.findAll()).thenReturn(users)

        val result = userService.getAll()
        assertEquals(users, result)
    }

    @Test
    fun `should return user by id`() {
        val user = User()
        `when`(userRepository.getReferenceById(1L)).thenReturn(user)

        val result = userService.getById(1L)
        assertEquals(user, result)
    }

    @Test
    fun `should throw exception when user not found by id`() {
        `when`(userRepository.getReferenceById(1L)).thenThrow(EntityNotFoundException::class.java)

        assertThrows(EntityNotFoundException::class.java) {
            userService.getById(1L)
        }
    }

    @Test
    fun `should add new user`() {
        val user = User()
        user.password = "password"
        `when`(passwordEncoder.encode(user.password)).thenReturn("encodedPassword")
        `when`(userRepository.save(user)).thenReturn(user)

        val result = userService.add(user)
        assertEquals("encodedPassword", result.password)
    }

    @Test
    fun `should update user`() {
        val user = User()
        `when`(userRepository.existsById(1L)).thenReturn(true)
        `when`(userRepository.save(user)).thenReturn(user)

        val result = userService.update(user, 1L)
        assertEquals(user, result)
    }

    @Test
    fun `should throw exception when updating non-existing user`() {
        val user = User()
        `when`(userRepository.existsById(1L)).thenReturn(false)

        assertThrows(EntityNotFoundException::class.java) {
            userService.update(user, 1L)
        }
    }

    @Test
    fun `should delete user by id`() {
        userService.delete(1L)
        verify(userRepository, times(1)).deleteById(1L)
    }

    @Test
    fun `should return user by login`() {
        val user = User()
        `when`(userRepository.findByLogin("testuser")).thenReturn(user)

        val result = userService.getByLogin("testuser")
        assertEquals(user, result)
    }

    @Test
    fun `should return null if user not found by login`() {
        `when`(userRepository.findByLogin("testuser")).thenReturn(null)

        val result = userService.getByLogin("testuser")
        assertNull(result)
    }

    @Test
    fun `should validate password`() {
        val dbUser = User()
        dbUser.password = "encodedPassword"
        val checkUser = User()
        checkUser.password = "rawPassword"
        `when`(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true)

        val result = userService.isValidPassword(dbUser, checkUser)
        assertTrue(result)
    }

    @Test
    fun `should return user roles`() {
        val roles = listOf(Role(), Role())
        `when`(userRoleRepository.findRoleByUserId(1L)).thenReturn(roles)

        val result = userService.getRoles(1L)
        assertEquals(roles, result)
    }
}
