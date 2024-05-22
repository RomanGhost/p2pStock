package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.Card
import com.example.p2p_project.models.Request
import com.example.p2p_project.models.User
import com.example.p2p_project.models.Wallet
import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.services.CardService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.WalletService
import com.example.p2p_project.services.dataServices.RequestStatusService
import com.example.p2p_project.services.dataServices.RequestTypeService
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(RequestController::class)
class RequestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var walletService: WalletService

    @MockBean
    private lateinit var cardService: CardService

    @MockBean
    private lateinit var requestService: RequestService

    @MockBean
    private lateinit var requestTypeService: RequestTypeService

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var requestStatusService: RequestStatusService

    private lateinit var user: User
    private lateinit var myUserDetails: MyUserDetails

    @BeforeEach
    fun setup() {
        user = User().apply {
            id = 1L
            login = "testuser"
            password = "password"
            isEnabled = true
        }

        myUserDetails = MyUserDetails(user, listOf())

        val authentication: Authentication =
            UsernamePasswordAuthenticationToken(myUserDetails, null, myUserDetails.authorities)
        val securityContext: SecurityContext = SecurityContextHolder.getContext()
        securityContext.authentication = authentication
    }

    @Test
    @WithMockUser
    fun `should show create request page`() {
        val wallets = listOf(Wallet())
        val cards = listOf(Card())
        val types = listOf(RequestType())

        `when`(walletService.getByUserId(user.id)).thenReturn(wallets)
        `when`(cardService.getByUserId(user.id)).thenReturn(cards)
        `when`(requestTypeService.getAll()).thenReturn(types.toMutableList())

        mockMvc.perform(get("/platform/request/add"))
            .andExpect(status().isOk)
            .andExpect(view().name("addRequest"))
            .andExpect(model().attribute("wallets", wallets))
            .andExpect(model().attribute("cards", cards))
            .andExpect(model().attribute("types", types))
    }

    @Test
    @WithMockUser
    fun `should save new request and redirect to welcome page`() {
        val request = Request().apply {
            pricePerUnit = 10.0
            quantity = 5.0
            wallet = Wallet()
            card = Card()
        }

        `when`(userService.getById(user.id)).thenReturn(user)

        mockMvc.perform(
            post("/platform/request/save")
                .flashAttr("request", request)
                .with(csrf())
        ) // добавляем CSRF токен
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/platform/account/welcome"))
    }

    // Добавьте больше тестов для других методов RequestController
}
