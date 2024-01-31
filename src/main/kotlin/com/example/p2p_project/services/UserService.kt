package com.example.p2p_project.services

import com.example.p2p_project.models.User
import com.example.p2p_project.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {
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
        return userRepository.findByLogin(login)
    }

    fun getByMail(email:String):User{
        return userRepository.findByEmail(email)
    }
    fun getByPhone(phone:String):User{
        return userRepository.findByPhone(phone)
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
        return userRepository.save(user)
    }
}