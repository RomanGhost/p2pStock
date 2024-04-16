package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.Request
import com.example.p2p_project.services.CardService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.WalletService
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
    private val userService: UserService
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
            redirectAttributes.addFlashAttribute("priceError", "Price can't be negative")
            return "redirect:/platform/request/add?error"
        }
        if(request.quantity <= 0.0){
            redirectAttributes.addFlashAttribute("quantityError", "Quantity can't be negative")
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

        val isBuying = request.requestType.name == "Покупка"
        model.addAttribute("isBuying", isBuying)

        //если заявка на покупку, то показать кошельки
        if(isBuying) {
            val wallets = walletService.getByUserId(userId)
            if(wallets.isNotEmpty())
                model.addAttribute("wallets", wallets)
        }
        else{
            val cards = cardService.getByUserId(userId)
            if (cards.isNotEmpty())
                model.addAttribute("cards", cards)
        }
        var isAccess = request.requestStatus.name == "Доступна на платформе"
        if (model.getAttribute("wallets")==null &&
            model.getAttribute("cards")==null){
            isAccess = false
        }
        if(request.user.id == userId){
            isAccess = false
        }
        model.addAttribute("isAccess", isAccess)
        return "requestInfo"
    }

    @GetMapping("/all")
    fun getAllRequest(model:Model):String{
        val requests = requestService.getByStatus("Доступна на платформе")
        model.addAttribute("requests", requests)

        return "allRequest"
    }
}