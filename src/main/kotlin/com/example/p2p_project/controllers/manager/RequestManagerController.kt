package com.example.p2p_project.controllers.manager

import com.example.p2p_project.errors.NotFoundException
import com.example.p2p_project.services.RequestService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/platform/manager/request")
class RequestManagerController(private val requestService: RequestService) {
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
    fun acceptRequestModeration(@PathVariable requestId:Long):String{
        if (!requestService.existById(requestId))
            throw NotFoundException()
        requestService.updateStatusById(requestId, "Доступна на платформе")
        return "redirect:/platform/manager/request/${requestId}"
    }

    @PostMapping("/{requestId}/moderation/change")
    fun changeRequestModeration(@PathVariable requestId:Long):String{
        if (!requestService.existById(requestId))
            throw NotFoundException()
        requestService.updateStatusById(requestId, "Отправлено на доработку")
        return "redirect:/platform/manager/request/${requestId}"
    }

    @PostMapping("/{requestId}/moderation/discard")
    fun discardRequestModeration(@PathVariable requestId:Long):String{
        if (!requestService.existById(requestId))
            throw NotFoundException()
        requestService.updateStatusById(requestId, "Закрыто: проблема")
        return "redirect:/platform/manager/request/${requestId}"
    }

}
