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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/deal")
class DealController(
    private val dealService: DealService,
    private val requestService: RequestService,
    private val requestTypeService: RequestTypeService,
    private val walletService: WalletService,
    private val cardService: CardService
) {
        @PostMapping("/add")
        fun addNewDeal(
            @RequestParam("requestId") requestId: Long,
            @RequestParam("walletId", required = false) walletId: Long?,
            @RequestParam("cardId", required = false) cardId: Long?,
            model:Model,
            authentication: Authentication
        ): String {
            val request = requestService.getById(requestId)
            val userDetails = authentication.principal as MyUserDetails
            val userId = userDetails.user.id

            if (request.user.id == userId) {
                //TODO(Выдать ошибку о том что пользователь не может быть контрагентом для себя)
            }

            //TODO(Вынести это в сервис)
            //Заявка контрагент
            val requestType:RequestType = if(request.requestType.name == "Продажа") {
                requestTypeService.getByName("Покупка")
            }else {
                requestTypeService.getByName("Продажа")
            }
            val wallet:Wallet? = if(walletId!=null) walletService.getById(walletId) else null
            val card: Card? = if(cardId!=null) cardService.getById(cardId) else null
            val pricePerUnit = request.pricePerUnit
            val quantity = request.quantity
            val description = "Заявка контрагент"
            val deadlineTime = request.deadlineDateTime

            var newRequest = Request(
                requestType=requestType,
                wallet=wallet,
                card=card,
                pricePerUnit=pricePerUnit,
                quantity=quantity,
                description=description,
                deadlineDateTime=deadlineTime
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
            print(resultDeal)

            return "stub"
        }

}