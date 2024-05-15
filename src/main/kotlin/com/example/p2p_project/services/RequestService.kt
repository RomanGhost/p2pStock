package com.example.p2p_project.services

import com.example.p2p_project.models.Request
import com.example.p2p_project.models.User
import com.example.p2p_project.repositories.RequestRepository
import com.example.p2p_project.repositories.dataTables.RequestStatusRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RequestService(val requestRepository: RequestRepository, val requestStatusRepository: RequestStatusRepository) {
    fun add(request: Request, requestStatusName:String=""):Request {
        if(requestStatusName != "")
            request.requestStatus = requestStatusRepository.findByName(requestStatusName)

        request.createDateTime = LocalDateTime.now()
        request.deadlineDateTime = request.createDateTime.plusDays(1)

        return requestRepository.save(request)
    }

    fun getAll(): List<Request>{
        return requestRepository.findAll()
    }

    fun getByStatus(status:String):List<Request>{
        return getAll().filter { it.requestStatus.name == status }
    }

    fun existById(id:Long): Boolean {
        return requestRepository.existsById(id)
    }

    fun getById(id: Long):Request{
        val request =
            try{
                requestRepository.getReferenceById(id)
            }
            catch (ex: JpaObjectRetrievalFailureException){
                throw EntityNotFoundException("Request with id: $id not found")
            }
        return request
    }

    fun delete(id:Long){
        return requestRepository.deleteById(id)
    }

    fun update(request: Request): Request {
        if (requestRepository.existsById(request.id))
            return requestRepository.save(request)
        else
            throw EntityNotFoundException("Request with id: ${request.id} not found")
    }

    fun updateStatusById(id:Long, requestStatusName:String):Request{
        if (!requestRepository.existsById(id))
            throw EntityNotFoundException("Request with id: $id not found")
        val request = requestRepository.getReferenceById(id)
        request.requestStatus = requestStatusRepository.findByName(requestStatusName)
        request.lastChangeStatusDateTime = LocalDateTime.now()
        return requestRepository.save(request)
    }

    fun getByUserId(userId:Long): List<Request> {
        return requestRepository.findByUserId(userId)
    }

    fun addManager(manager: User, requestId: Long): Request {
        val request = requestRepository.getReferenceById(requestId)
        request.managerId = manager
        return requestRepository.save(request)
    }
}