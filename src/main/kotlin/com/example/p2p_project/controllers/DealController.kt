package com.example.p2p_project.controllers

import com.example.p2p_project.repositories.DealRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/deal")
class DealController(
    private val dealRepository: DealRepository
) {

}