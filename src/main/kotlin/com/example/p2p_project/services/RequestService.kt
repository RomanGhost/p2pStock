package com.example.p2p_project.services

import com.example.p2p_project.models.Request
import com.example.p2p_project.repositories.RequestRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service

@Service
class RequestService(val requestRepository: RequestRepository) {
    fun add(request: Request):Request {
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
}