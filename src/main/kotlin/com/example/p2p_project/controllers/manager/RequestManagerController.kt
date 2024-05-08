package com.example.p2p_project.controllers.manager

import com.example.p2p_project.errors.NotFoundException
import com.example.p2p_project.services.AuthenticationService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.UserService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/manager/request")
class RequestManagerController(
    private val requestService: RequestService,
    private val userService: UserService,
    private val authenticationService: AuthenticationService
) {
    @GetMapping("/{requestId}")
    fun getRequestManager(@PathVariable requestId:Long, model: Model):String{
        if (!requestService.existById(requestId))
            throw NotFoundException()
        val  request = requestService.getById(requestId)
        model.addAttribute("request", request)

        val isModeration = request.requestStatus.name == "Модерация"
        model.addAttribute("isModeration", isModeration)

        return "requestInfo"
    }

    @GetMapping("/all")
    fun getAllRequestsManager(@RequestParam("status_filter", required = false) status:String?=null,  model: Model):String{
        var  requests = requestService.getAll()
            if (status != null)
                requests = requests.filter { it.requestStatus.name == status }

        model.addAttribute("requests", requests)
        model.addAttribute("isManager", true)
        return "allRequest"
    }

    //TODO Сделать проверку, сущечтвует ли такая заявка
    @PostMapping("/{requestId}/moderation/accept")
    fun acceptRequestModeration(@PathVariable requestId: Long, authentication: Authentication): String {
        if (!requestService.existById(requestId))
            throw NotFoundException()

        val userDetails = authenticationService.getUserDetails(authentication)
        val manager = userService.getById(userDetails.user.id)
        requestService.addManager(manager, requestId)

        requestService.updateStatusById(requestId, "Доступна на платформе")
        return "redirect:/manager/request/${requestId}"
    }

    @PostMapping("/{requestId}/moderation/change")
    fun changeRequestModeration(@PathVariable requestId: Long, authentication: Authentication): String {
        if (!requestService.existById(requestId))
            throw NotFoundException()

        val userDetails = authenticationService.getUserDetails(authentication)
        val manager = userService.getById(userDetails.user.id)
        requestService.addManager(manager, requestId)

        requestService.updateStatusById(requestId, "Отправлено на доработку")
        return "redirect:/manager/request/${requestId}"
    }

    @PostMapping("/{requestId}/moderation/discard")
    fun discardRequestModeration(@PathVariable requestId: Long, authentication: Authentication): String {
        if (!requestService.existById(requestId))
            throw NotFoundException()

        val userDetails = authenticationService.getUserDetails(authentication)
        val manager = userService.getById(userDetails.user.id)
        requestService.addManager(manager, requestId)

        requestService.updateStatusById(requestId, "Закрыто: проблема")
        return "redirect:/manager/request/${requestId}"
    }

}

