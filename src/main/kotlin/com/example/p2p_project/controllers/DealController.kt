package com.example.p2p_project.controllers

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.*
import com.example.p2p_project.models.dataTables.DealStatus
import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.services.*
import com.example.p2p_project.services.dataServices.RequestTypeService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Controller
@RequestMapping("/platform/deal")
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

        val deal = dealService.getById(dealId)
        model.addAttribute("deal", deal)

        val showAcceptButton = dealService.acceptDeal(deal, userDetails.user)
        model.addAttribute("showAcceptButton", showAcceptButton)

        val showConfirmPaymentButton = dealService.confirmPayment(deal, userDetails.user)
        model.addAttribute("showConfirmPaymentButton", showConfirmPaymentButton)

        val showConfirmReceiptButton = dealService.confirmReceipt(deal, userDetails.user)
        model.addAttribute("showConfirmReceiptButton", showConfirmReceiptButton)

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
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot be a counterparty to themselves")
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

        initialRequest = requestService.updateStatusById(requestId, newStatus)

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

    //Buttons success
    @PostMapping("/{dealId}/accept")
    fun acceptDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val deal = dealService.getById(dealId)

        if (!dealService.acceptDeal(deal, userDetails.user)) {
            return "redirect:/deal/${dealId}"
        }

        dealService.updateStatus(dealId, "Ожидание перевода")
        return "redirect:/deal/${dealId}"
    }

    @PostMapping("/{dealId}/confirm_payment")
    fun confirmPaymentDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val deal = dealService.getById(dealId)

        if (!dealService.confirmPayment(deal, userDetails.user)) {
            return "redirect:/deal/${dealId}"
        }

        dealService.updateStatus(dealId, "Ожидание подтверждения перевода")
        return "redirect:/deal/${dealId}"
    }

    @PostMapping("/{dealId}/confirm_receipt_payment")
    fun confirmReceiptPaymentDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val deal = dealService.getById(dealId)

        if (!dealService.confirmReceipt(deal, userDetails.user)) {
            return "redirect:/deal/${dealId}"
        }

        val newStatus = "Закрыто: успешно"
        requestService.updateStatusById(deal.sellRequest.id!!, newStatus)
        requestService.updateStatusById(deal.buyRequest.id!!, newStatus)

        dealService.updateStatus(dealId, newStatus)
        return "redirect:/deal/${dealId}"
    }

    // Buttons failure
    //TODO("Реализовать проблемные места(см. бизнес процесс)")


}