package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.RegisterUser
import com.example.p2p_stock.dataclasses.UserForAdmin
import com.example.p2p_stock.dataclasses.UserInfo
import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.models.Role
import com.example.p2p_stock.models.User
import com.example.p2p_stock.repositories.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder
) {

    fun findAll(): List<User> = userRepository.findAll()

    fun findByFilters(
        id: Long?,
        isActive: Boolean?,
        updatedAfter: LocalDateTime?,
        updatedBefore: LocalDateTime?,
        adminEmail: String,
        pageable: Pageable
    ): Page<User> {
        // Получаем данные с учетом пагинаци
        val page = userRepository.findByFilters(id, isActive, adminEmail, pageable)
        return page
    }

    fun findById(id: Long): User? = userRepository.findById(id).orElse(null)

    fun findByUsername(login: String): User? = userRepository.findByLogin(login).orElse(null)

    fun save(user: User): User {
        user.updatedAt = LocalDateTime.now()
        return userRepository.save(user)
    }


    fun register(registerUser: RegisterUser):User{
        // Проверка на наличие существующего пользователя
        if (userRepository.existsByLogin(registerUser.login)) {
            throw IllegalArgumentException("Username is already taken")
        }

        if (userRepository.existsByEmail(registerUser.email)) {
            throw IllegalArgumentException("Email is already registered")
        }

        // Получение роли (вы можете изменить логику поиска роли)
        val userRole = roleService.getUserRole().orElseThrow {
            IllegalArgumentException("User role not found")
        }

        // Хеширование пароля
        val encodedPassword = passwordEncoder.encode(registerUser.password)

        // Создание нового пользователя
        val newUser = User(
            login = registerUser.login,
            email = registerUser.email,
            password = encodedPassword,
            role = userRole
        )

        // Сохранение пользователя в базе данных
        return userRepository.save(newUser)
    }

    // Пример поиска пользователя по email (для логина)
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email).get()
    }

    fun delete(id: Long) = userRepository.deleteById(id)

    // Новый метод для получения роли пользователя
    fun getUserRole(userId: Long): Role? {
        return userRepository.findById(userId).orElse(null)?.role
    }

    fun blockUser(user:User): User {
        return if(user.isActive){
            user.isActive = false
            save(user)
        } else{
            user
        }
    }

    fun unblockUser(user:User): User {
        return if(!user.isActive){
            user.isActive = true
            save(user)
        } else{
            user
        }
    }

    fun userToUserInfo(user:User): UserInfo {
        return UserInfo(
            id = user.id,
            login = user.login,
            email = user.email,
            roleName = user.role!!.name
        )
    }

    fun userToUserForAdmin(user:User):UserForAdmin {
        return UserForAdmin(
            user.id,
            user.login,
            user.email,
            user.role!!.name,
            user.isActive,
            user.updatedAt.toString(),
        )
    }
}
