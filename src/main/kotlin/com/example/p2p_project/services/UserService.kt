package com.example.p2p_project.services

import com.example.p2p_project.models.User
import com.example.p2p_project.models.dataTables.Role
import com.example.p2p_project.repositories.UserRepository
import com.example.p2p_project.repositories.adjacent.UserRoleRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository,
                  val userRoleRepository:UserRoleRepository,
    val passwordEncoder: PasswordEncoder
) {
    fun getAll():List<User>{
        return userRepository.findAll()
    }

    fun getById(id:Long):User{
        val user = try{
            userRepository.getReferenceById(id)
        }catch (ex: JpaObjectRetrievalFailureException){
            throw EntityNotFoundException("User with id: $id not found")
        }
        return user
    }

    fun getByLogin(login:String):User{
        val user = try{
            userRepository.findByLogin(login)
        }catch (ex: JpaObjectRetrievalFailureException){
            throw EntityNotFoundException("User with login: $login not found")
        }
        return user
    }

    fun getByMail(email:String):User{
        val user = try{
            userRepository.findByEmail(email)
        }catch (ex: JpaObjectRetrievalFailureException){
            throw EntityNotFoundException("User with email: $email not found")
        }
        return user
    }
    fun getByPhone(phone:String):User{
        val user = try{
            userRepository.findByPhone(phone)
        }catch (ex: JpaObjectRetrievalFailureException){
            throw EntityNotFoundException("User with phone: $phone not found")
        }
        return user
    }

    fun update(user:User,id:Long):User{
        user.id = id
        if (userRepository.existsById(id))
            return userRepository.save(user)
        else
            throw EntityNotFoundException("User with id: $id not found")

    }

    fun delete(id:Long){
        return userRepository.deleteById(id)
    }

    fun add(user:User):User{
        user.password = passwordEncoder.encode(user.password)
        return userRepository.save(user)
    }

    fun getRoles(id: Long): List<Role> {
        return userRoleRepository.findRoleByUserId(id)
    }
}