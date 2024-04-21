package com.example.p2p_project.controllers.manager

import com.example.p2p_project.services.DealService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/platform/manager/deal")
class DealManagerController(private val dealService: DealService) {
    @GetMapping("/{dealId}")
    fun getDealForManager(@PathVariable dealId:Long, model:Model):String{
        val deal = dealService.getById(dealId)
        model.addAttribute("deal", deal)

        return "dealInfo"
    }
}