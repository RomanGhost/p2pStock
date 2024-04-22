package com.example.p2p_project.controllers.manager

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.adjacent.DealAction
import com.example.p2p_project.services.DealService
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.adjacent.DealActionService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/platform/manager/deal")
class DealManagerController(
    private val dealService: DealService,
    private val dealActionService: DealActionService,
    private val userService: UserService
) {
    @GetMapping("/{dealId}")
    fun getDealForManager(@PathVariable dealId:Long, model:Model):String{
        val deal = dealService.getById(dealId)
        model.addAttribute("deal", deal)

        val showTakeInWorkButton = deal.status.name == "Приостановлено: решение проблем"
        model.addAttribute("showTakeInWorkButton", showTakeInWorkButton)

        val showDealActionFrom = deal.status.name == "Ожидание решения менеджера"
        model.addAttribute("showDealActionFrom", showDealActionFrom)
        if(showDealActionFrom)
            model.addAttribute("dealAction", DealAction())

        return "dealInfo"
    }

    @PostMapping("/{dealId}/take_in_work")
    fun postTakeInWork(@PathVariable dealId:Long, model:Model, authentication: Authentication):String{
        dealService.updateStatus(dealId, "Ожидание решения менеджера")

        return "redirect:/platform/manager/deal/$dealId"
    }
    @PostMapping("/{dealId}/action")
    fun postActionDeal(@PathVariable dealId:Long,
                       @ModelAttribute("dealAction") dealAction: DealAction,
                       model:Model,
                       authentication: Authentication
    ):String{
        val deal = dealService.getById(dealId)
        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id
        val user = userService.getById(authUserId)

        dealAction.deal = deal
        dealAction.user = user
        val newDealAction = dealActionService.add(dealAction)
        val price = deal.buyRequest.pricePerUnit * deal.buyRequest.quantity
            if (price < 1000) dealActionService.setPriority(newDealAction,"Низкий")
            else if (price < 10000) dealActionService.setPriority(newDealAction,"Средний")
            else dealActionService.setPriority(newDealAction,"Высокий")

        if(dealAction.confirmation){
            dealService.updateStatus(dealId, "Ожидание подтверждения перевода")
        }else{
            dealService.updateStatus(dealId, "Закрыто: отменена менеджером")
        }

        return "redirect:/platform/manager/deal/$dealId"
    }


}