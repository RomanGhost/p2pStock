package com.example.p2p_project.controllers

import com.example.p2p_project.models.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${application.info.appLink}/request")
class RequestController {
   @Value("\${application.info.appLink}")
    private lateinit var appLink: String

    @GetMapping("/create")
    fun createRequest(model: Model):String{
        model.addAttribute("request", Request())
        //TODO("Выборка значений для пользователя из таблиц Карты и Кошельки")
        return "requestCreate"
    }
}