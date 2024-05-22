package com.example.p2p_project.controllers.manager

import com.example.p2p_project.errors.NotFoundException
import com.example.p2p_project.services.AuthenticationService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.dataServices.RequestStatusService
import com.example.p2p_project.services.dataServices.RequestTypeService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/manager/request")
class RequestManagerController(
    private val requestService: RequestService,
    private val requestStatusService: RequestStatusService,
    private val requestTypeService: RequestTypeService,
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
    fun getAllRequestsManager(
        @RequestParam("sort_by", required = false) sortBy: String? = null,
        @RequestParam("sort_order", required = false) sortOrder: String? = null,
        @RequestParam("request_type", required = false) requestType: String? = null,
        @RequestParam("request_status", required = false) requestStatus: String? = null,
        model: Model
    ): String {
        var requests = requestService.getAll()

        if (!requestType.isNullOrEmpty()) {
            requests = requests.filter { it.requestType.id == requestType.toLong() }
        }

        if (!requestStatus.isNullOrEmpty()) {
            requests = requests.filter { it.requestStatus.id == requestStatus.toLong() }
        }

        if (sortBy != null) {
            requests = when (sortBy) {
                "pricePerUnit" -> requests.sortedBy { it.pricePerUnit }
                "quantity" -> requests.sortedBy { it.quantity }
                "createDateTime" -> requests.sortedBy { it.createDateTime }
                else -> requests
            }
            if (sortOrder == "desc") {
                requests = requests.reversed()
            }
        }

        val requestStatuses = requestStatusService.getAll()
        model.addAttribute("requestsStatuses", requestStatuses)

        val requestTypes = requestTypeService.getAll()
        model.addAttribute("requestsTypes", requestTypes)

        model.addAttribute("requests", requests)
        model.addAttribute("isManager", true)
        return "allRequest"
    }

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

