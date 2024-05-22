package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.User
import com.example.p2p_project.models.Wallet
import com.example.p2p_project.models.dataTables.Cryptocurrency
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.WalletService
import com.example.p2p_project.services.dataServices.CryptocurrencyService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(WalletController::class)
class WalletControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var walletService: WalletService

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var cryptocurrencyService: CryptocurrencyService

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
    fun `should show add wallet page`() {
        val cryptocurrencies = listOf<Any>() // add mock cryptocurrency list if needed
        `when`(cryptocurrencyService.getAll()).thenReturn(cryptocurrencies as List<Cryptocurrency>?)

        mockMvc.perform(get("/platform/wallet/add"))
            .andExpect(status().isOk)
            .andExpect(view().name("addWallet"))
            .andExpect(model().attributeExists("cryptocurrencies"))
            .andExpect(model().attributeExists("wallet"))
    }

    @Test
    fun `should redirect to add wallet with error when wallet name is too short`() {
        val wallet = Wallet()
        wallet.name = "W"

        mockMvc.perform(
            post("/platform/wallet/save")
                .with(csrf())
                .flashAttr("wallet", wallet)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/platform/wallet/add?error"))
            .andExpect(flash().attribute("errorMessage", "Wallet name is too short"))
    }

    @Test
    fun `should redirect to add wallet with error when wallet name exists`() {
        val wallet = Wallet()
        wallet.name = "ValidName"
        wallet.user = user

        `when`(walletService.existWalletForUserId(wallet.name, wallet.user.id)).thenReturn(true)

        mockMvc.perform(
            post("/platform/wallet/save")
                .with(csrf())
                .flashAttr("wallet", wallet)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/platform/wallet/add?error"))
            .andExpect(flash().attribute("errorMessage", "Wallet name is exist"))
    }

    @Test
    fun `should redirect to welcome page after successful wallet addition`() {
        val wallet = Wallet()
        wallet.name = "ValidName"
        wallet.user = user

        `when`(walletService.existWalletForUserId(wallet.name, wallet.user.id)).thenReturn(false)
        `when`(userService.getById(user.id)).thenReturn(user)

        mockMvc.perform(
            post("/platform/wallet/save")
                .with(csrf())
                .flashAttr("wallet", wallet)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/platform/account/welcome"))
    }

    @Test
    fun `should delete wallet and redirect to welcome page`() {
        mockMvc.perform(
            get("/platform/wallet/delete/1")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/platform/account/welcome"))

        verify(walletService, times(1)).deleteById(1L)
    }
}
