package com.example.p2p_project.controllers

import com.example.p2p_project.models.Deal
import com.example.p2p_project.services.DealService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/deal")
class DealController(val dealService: DealService) {
    @PostMapping("/add")
    fun add(@RequestBody deal: Deal):ResponseEntity<Deal>{
        var newDeal = dealService.add(deal)
        return ResponseEntity<Deal>(newDeal, HttpStatus.CREATED)
    }

    @PutMapping("/update")
    fun update(@RequestBody deal:Deal, @RequestParam(required = false) id:Long):ResponseEntity<Deal>{
        var updateDeal = dealService.update(deal, id)
        return ResponseEntity(updateDeal, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{dealId}")
    fun delete(@PathVariable dealId:Long):ResponseEntity<Deal>{
        dealService.delete(dealId)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/all")
    fun getAll():ResponseEntity<List<Deal>>{
        val dealList:List<Deal> = dealService.getAll()
        return ResponseEntity(dealList, HttpStatus.OK)
    }

    @GetMapping("/{dealID}")
    fun getByID(@PathVariable dealID:Long):ResponseEntity<Deal>{
        val deal:Deal = dealService.getById(dealID)
        return ResponseEntity(deal, HttpStatus.FOUND)
    }

}