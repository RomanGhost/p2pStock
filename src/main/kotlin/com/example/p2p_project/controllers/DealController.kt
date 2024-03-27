package com.example.p2p_project.controllers

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.Card
import com.example.p2p_project.models.Deal
import com.example.p2p_project.models.Request
import com.example.p2p_project.models.Wallet
import com.example.p2p_project.models.dataTables.DealStatus
import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.services.CardService
import com.example.p2p_project.services.DealService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.WalletService
import com.example.p2p_project.services.dataServices.RequestTypeService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/deal")
class DealController(
    private val dealService: DealService,
    private val requestService: RequestService,
    private val requestTypeService: RequestTypeService,
    private val walletService: WalletService,
    private val cardService: CardService
) {
    @GetMapping("/{dealId}")
    fun getDealInfo(@PathVariable dealId:Long, model: Model, authentication: Authentication):String{
        val deal = dealService.getById(dealId)

        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id

        val canLooking = deal.buyRequest.user.id == authUserId || deal.sellRequest.user.id == authUserId
        if (!canLooking) {
            //TODO(Выдать ошибку о том что пользователь не может просматривать заявку(status 406))
        }
        model.addAttribute("deal", deal)
        return "dealInfo"

    }
    @PostMapping("/add")
    fun addNewDeal(
        @RequestParam("requestId") requestId: Long,
        @RequestParam("walletId", required = false) walletId: Long?,
        @RequestParam("cardId", required = false) cardId: Long?,
        authentication: Authentication
    ): String {
        val request = requestService.getById(requestId)
        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id

        if (request.user.id == authUserId) {
            //TODO(Выдать ошибку о том что пользователь не может быть контрагентом для себя)
        }

        //Заявка контрагент
        val requestType:RequestType = if(request.requestType.name == "Продажа") {
            requestTypeService.getByName("Покупка")
        }else {
            requestTypeService.getByName("Продажа")
        }
        val wallet:Wallet? = if(walletId!=null) walletService.getById(walletId) else null
        val card: Card? = if(cardId!=null) cardService.getById(cardId) else null

        var newRequest = Request(
            requestType=requestType,
            wallet=wallet,
            card=card,
            pricePerUnit=request.pricePerUnit,
            quantity=request.quantity,
            description="Заявка контрагент",
            deadlineDateTime=request.deadlineDateTime
        )

        val newStatus = "Используется в сделке"
        newRequest = requestService.add(newRequest, newStatus)

        val initialRequest = requestService.updateStatus(request, requestId, newStatus)

        //Создание сделки на основе заявок
        //Если исходная заявка на продажу
        val isBuyCreated = initialRequest.requestType.name == "Продажа"
        val dealStatus = DealStatus(name="Подтверждение сделки")

        lateinit var sellRequest: Request
        lateinit var buyRequest: Request
        if(isBuyCreated){
            sellRequest = newRequest
            buyRequest = initialRequest
        }
        else{
            sellRequest = initialRequest
            buyRequest = newRequest
        }
        val deal = Deal(
            status = dealStatus,
            closeDateTime = initialRequest.deadlineDateTime,
            isBuyCreated = isBuyCreated,
            sellRequest = sellRequest,
            buyRequest = buyRequest
        )


        val resultDeal = dealService.add(deal)


        return "redirect:/deal/${resultDeal.id}"
    }

}