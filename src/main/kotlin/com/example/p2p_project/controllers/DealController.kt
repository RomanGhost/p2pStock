package com.example.p2p_project.controllers

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.*
import com.example.p2p_project.models.dataTables.DealStatus
import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.services.*
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
    private val cardService: CardService,
    private val userService: UserService,
    private val authenticationService: AuthenticationService
    ) {
    @GetMapping("/{dealId}")
    fun getDealInfo(@PathVariable dealId:Long, model: Model, authentication: Authentication):String{
        val userDetails = authenticationService.getUserDetails(authentication)
        val authUserId = userDetails.user.id

        if (!dealService.checkDealAccess(dealId, authUserId)) {
            return "redirect:/platform/account/welcome"
        }

        val showAcceptButton = dealService.initiatorAccept(dealId, authUserId, "Подтверждение сделки")
        model.addAttribute("showAcceptButton", showAcceptButton)

        val showConfirmPaymentButton = dealService.counterpartyAccept(dealId, authUserId, "Ожидание перевода")
        model.addAttribute("showConfirmPaymentButton", showConfirmPaymentButton)

        val deal = dealService.getById(dealId)
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
        var initialRequest: Request = requestService.getById(requestId)
        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id

        if (initialRequest.user.id == authUserId) {
            //TODO(Выдать ошибку о том что пользователь не может быть контрагентом для себя)
        }

        //Заявка контрагент
        val requestType:RequestType = if(initialRequest.requestType.name == "Продажа") {
            requestTypeService.getByName("Покупка")
        }else {
            requestTypeService.getByName("Продажа")
        }
        val wallet:Wallet? = if(walletId!=null) walletService.getById(walletId) else null
        val user: User = userService.getById(authUserId)
        val card: Card? = if(cardId!=null) cardService.getById(cardId) else null

        var newRequest = Request(
            requestType=requestType,
            wallet=wallet,
            user=user,
            card=card,
            pricePerUnit=initialRequest.pricePerUnit,
            quantity=initialRequest.quantity,
            description="Заявка контрагент ${initialRequest.id}",
            deadlineDateTime=initialRequest.deadlineDateTime
        )

        val newStatus = "Используется в сделке"
        newRequest = requestService.add(newRequest, newStatus)

        initialRequest = requestService.updateStatus(initialRequest, requestId, newStatus)

        //Создание сделки на основе заявок
        //Если исходная заявка на продажу
        val isBuyCreated = initialRequest.requestType.name == "Продажа"
        val dealStatus = DealStatus(name="Подтверждение сделки")

        lateinit var sellRequest: Request
        lateinit var buyRequest: Request
        if(isBuyCreated){
            sellRequest = initialRequest
            buyRequest = newRequest
        }
        else{
            sellRequest = newRequest
            buyRequest = initialRequest
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

    @PostMapping("/{dealId}/accept")
    fun acceptDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val authUserId = userDetails.user.id

        if (!dealService.initiatorAccept(dealId, authUserId, "Подтверждение сделки")) {
            return "redirect:/deal/${dealId}"
        }

        dealService.updateStatus(dealId, "Ожидание перевода")
        return "redirect:/deal/${dealId}"
    }

    @PostMapping("/{dealId}/confirm_payment")
    fun confirmPaymentDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val authUserId = userDetails.user.id

        if (!dealService.counterpartyAccept(dealId, authUserId, "Ожидание перевода")) {
            return "redirect:/deal/${dealId}"
        }

        dealService.updateStatus(dealId, "Ожидание подтверждения перевода")
        return "redirect:/deal/${dealId}"
    }
}