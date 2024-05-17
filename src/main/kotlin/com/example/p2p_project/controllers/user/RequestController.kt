package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.Request
import com.example.p2p_project.models.Wallet
import com.example.p2p_project.services.CardService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.WalletService
import com.example.p2p_project.services.dataServices.RequestStatusService
import com.example.p2p_project.services.dataServices.RequestTypeService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/platform/request")
class RequestController(
    private val walletService: WalletService,
    private val cardService: CardService,
    private val requestService: RequestService,
    private val requestTypeService: RequestTypeService,
    private val userService: UserService,
    private val requestStatusService: RequestStatusService
) {


    @GetMapping("/add")
    fun getCreateRequest(model: Model, authentication: Authentication, redirectAttributes: RedirectAttributes):String{
        val userDetails = authentication.principal as MyUserDetails
        val userId = userDetails.user.id

        val cards = cardService.getByUserId(userId)
        val wallets = walletService.getByUserId(userId)
        val types = requestTypeService.getAll()
        val request = Request()

        model.addAttribute("request", request)
        model.addAttribute("cards", cards)
        model.addAttribute("wallets", wallets)
        model.addAttribute("types", types)

        return "addRequest"
    }

    @PostMapping("/save")
    fun saveNewRequest(@ModelAttribute request: Request,
                       authentication: Authentication,
                       redirectAttributes: RedirectAttributes):String{
        if(request.pricePerUnit <= 0.0){
            redirectAttributes.addFlashAttribute("priceError", "Цена не может быть отрицательной")
            return "redirect:/platform/request/add?error"
        }
        if(request.quantity <= 0.0){
            redirectAttributes.addFlashAttribute("quantityError", "Количество не может быть отрицательной")
            return "redirect:/platform/request/add?error"
        }

        if(request.wallet == null){
            redirectAttributes.addFlashAttribute("walletError", "Кошелек не выбран!")
            return "redirect:/platform/request/add?error"
        }

        if(request.card == null){
            redirectAttributes.addFlashAttribute("cardError", "Карта не выбрана!")
            return "redirect:/platform/request/add?error"
        }

        val userDetails = authentication.principal as MyUserDetails
        val userId = userDetails.user.id
        val user = userService.getById(userId)
        request.user = user

        requestService.add(request, "Модерация")
        return "redirect:/platform/account/welcome"
    }

    @GetMapping("/{requestId}")
    fun getRequest(@PathVariable requestId:Long, model:Model, authentication: Authentication):String{
        val request = requestService.getById(requestId)

        val userDetails = authentication.principal as MyUserDetails
        val userId = userDetails.user.id
        model.addAttribute("request", request)

        if (request.wallet == null || request.card == null) {
            requestService.updateStatusById(requestId, "Закрыто: неактуально")
            return "redirect:/platform/request/all"
        }

        val isUserInitiator = request.user.id == userId
        model.addAttribute("isUserInitiator", isUserInitiator)

        if (!isUserInitiator) {
            val userWallets = walletService.getByUserId(userId)
            val wallets: List<Wallet> =
                userWallets.filter { it.cryptocurrency.id == request.wallet?.cryptocurrency?.id }

            if(wallets.isNotEmpty())
                model.addAttribute("wallets", wallets)

            val cards = cardService.getByUserId(userId)
            if (cards.isNotEmpty())
                model.addAttribute("cards", cards)

            var isAccess = request.requestStatus.name == "Доступна на платформе"
            if (cards.isEmpty() || wallets.isEmpty()) {
                isAccess = false
            }
            model.addAttribute("isAccess", isAccess)
        }


        val requestEditStatus = request.requestStatus.name == "Отправлено на доработку"
        model.addAttribute("edit", requestEditStatus)

        return "requestInfo"
    }

    @GetMapping("/{requestId}/cancel")
    fun postCancelRequest(@PathVariable requestId:Long, model:Model, authentication: Authentication):String {
        requestService.updateStatusById(requestId, "Закрыто: неактуально")
        return "redirect:/platform/request/$requestId"
    }

    @GetMapping("/all")
    fun getAllRequest(
        @RequestParam("sort_by", required = false) sortBy: String? = null,
        @RequestParam("sort_order", required = false) sortOrder: String? = null,
        @RequestParam("request_type", required = false) requestType: String? = null,
        model: Model
    ): String {
        var requests = requestService.getByStatus("Доступна на платформе")

        if (!requestType.isNullOrEmpty()) {
            requests = requests.filter { it.requestType.id == requestType.toLong() }
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

        return "allRequest"
    }

    @PostMapping("/{requestId}/update")
    fun updateRequest(
        @PathVariable("requestId") requestId: Long,
        @ModelAttribute request: Request,
        redirectAttributes: RedirectAttributes
    ): String {
        val oldRequest = requestService.getById(requestId)
        if (oldRequest.description == request.description) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заявка не имеет изменений")
            return "redirect:/platform/request/$requestId?error"
        }
        oldRequest.description = request.description
        requestService.update(oldRequest)
        requestService.updateStatusById(requestId, "Модерация")
        return "redirect:/platform/request/$requestId"
    }
}
