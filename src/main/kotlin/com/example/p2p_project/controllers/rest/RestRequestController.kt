package com.example.p2p_project.controllers.rest

import com.example.p2p_project.models.Request
import com.example.p2p_project.services.RequestService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${application.info.appLink}/request")
class RestRequestController(val requestService: RequestService) {
    @PostMapping("/add")
    fun add(@RequestBody request: Request): ResponseEntity<Request> {
        val newRequest = requestService.add(request)
        return ResponseEntity(newRequest, HttpStatus.CREATED)
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<Request>> {
        val requestList = requestService.getAll()
        return ResponseEntity(requestList, HttpStatus.OK)
    }

    @GetMapping("/{requestId}")
    fun getById(@PathVariable requestId: Long): ResponseEntity<Request> {
        val request = requestService.getById(requestId)
        return ResponseEntity(request, HttpStatus.OK)
    }

    @PutMapping("/update")
    fun update(@RequestBody request: Request, @RequestParam id: Long): ResponseEntity<Request> {
        val updateRequest = requestService.update(request, id)
        return ResponseEntity(updateRequest, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{requestId}")
    fun delete(@PathVariable requestId: Long): ResponseEntity<Request> {
        requestService.delete(requestId)
        return ResponseEntity(HttpStatus.OK)
    }
}