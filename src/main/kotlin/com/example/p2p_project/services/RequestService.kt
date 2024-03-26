package com.example.p2p_project.services

import com.example.p2p_project.models.Request
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

    fun update(request: Request, id:Long):Request{
        request.id = id
        if (requestRepository.existsById(id))
            return requestRepository.save(request)
        else
            throw EntityNotFoundException("Request with id: $id not found")

    }

    fun updateStatus(request: Request, id:Long, requestStatusName:String):Request{
        if (!requestRepository.existsById(id))
            throw EntityNotFoundException("Request with id: $id not found")

        request.id = id
        request.requestStatus = requestStatusRepository.findByName(requestStatusName)
        return requestRepository.save(request)
    }

    fun getByUserId(userId:Long): List<Request> {
        return requestRepository.findByUserId(userId)
    }
}