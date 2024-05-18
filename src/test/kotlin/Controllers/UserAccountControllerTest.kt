package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.*
import com.example.p2p_project.services.CardService
import com.example.p2p_project.services.DealService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.WalletService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UserAccountController::class)
class UserAccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var walletService: WalletService

    @MockBean
    private lateinit var cardService: CardService

    @MockBean
    private lateinit var requestService: RequestService

    @MockBean
    private lateinit var dealService: DealService

    private lateinit var user: User
    private lateinit var myUserDetails: MyUserDetails

    @BeforeEach
    fun setup() {
        user = User()
        user.id = 1L
        user.login = "testuser"
        user.password = "password"
        user.isEnabled = true

        myUserDetails = MyUserDetails(user, listOf())

        val authentication: Authentication =
            UsernamePasswordAuthenticationToken(myUserDetails, null, myUserDetails.authorities)
        val securityContext: SecurityContext = SecurityContextHolder.getContext()
        securityContext.authentication = authentication
    }

    @Test
    @WithMockUser
    fun `should show welcome page with user data`() {
        val wallets = listOf(Wallet())
        val cards = listOf(Card())
        val requests = listOf(Request())
        val deals = listOf(Deal())

        `when`(walletService.getByUserId(user.id)).thenReturn(wallets)
        `when`(cardService.getByUserId(user.id)).thenReturn(cards)
        `when`(requestService.getByUserId(user.id)).thenReturn(requests)
        `when`(dealService.getByUserId(user.id)).thenReturn(deals)

        mockMvc.perform(get("/platform/account/welcome"))
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("login", user.login))
            .andExpect(model().attribute("cards", cards))
            .andExpect(model().attribute("wallets", wallets))
            .andExpect(model().attribute("requests", requests))
            .andExpect(model().attribute("deals", deals))
    }
}
