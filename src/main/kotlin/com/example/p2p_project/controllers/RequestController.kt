package com.example.p2p_project.controllers

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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/request")
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
            return "redirect:/request/add?error"
        }
        if(request.quantity <= 0.0){
            redirectAttributes.addFlashAttribute("quantityError", "Quantity can't be negative")
            return "redirect:/request/add?error"
        }


        val userDetails = authentication.principal as MyUserDetails
        val userId = userDetails.user.id
        val user = userService.getById(userId)
        request.user = user

        requestService.add(request, "Модерация")
        return "redirect:/account/welcome"
    }
}