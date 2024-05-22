package com.example.p2p_project.controllers.user

import com.example.p2p_project.config.MyUserDetails
import com.example.p2p_project.models.*
import com.example.p2p_project.models.dataTables.RequestType
import com.example.p2p_project.services.*
import com.example.p2p_project.services.dataServices.DealStatusService
import com.example.p2p_project.services.dataServices.RequestTypeService
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/platform/deal")
class DealController(
    private val dealService: DealService,
    private val dealStatusService: DealStatusService,
    private val requestService: RequestService,
    private val requestTypeService: RequestTypeService,
    private val walletService: WalletService,
    private val cardService: CardService,
    private val userService: UserService,
    private val authenticationService: AuthenticationService
    ) {
    @GetMapping("/{dealId}")
    fun getDealInfo(@PathVariable dealId: Long, model: Model, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val authUserId = userDetails.user.id


        if (!dealService.checkDealAccess(dealId, authUserId)) {
            return "redirect:/platform/account/welcome"
        }

        val deal = dealService.getById(dealId)
        model.addAttribute("deal", deal)


        //Штатные ситуации
        //Подтвердить создание сделки
        val showAcceptButton = dealService.acceptDeal(deal, userDetails.user)
        model.addAttribute("showAcceptButton", showAcceptButton)

        //Подтвердить отправку средств
        val showConfirmPaymentButton = dealService.confirmPayment(deal, userDetails.user)
        model.addAttribute("showConfirmPaymentButton", showConfirmPaymentButton)

        //Подтвердить получение средств
        val showConfirmReceiptButton = dealService.confirmReceipt(deal, userDetails.user)
        model.addAttribute("showConfirmReceiptButton", showConfirmReceiptButton)

        //Нештатные ситуации проблем
        //Отклонить сделку
        val showRefuseButton = dealService.refuseDeal(deal)
        model.addAttribute("showRefuseButton", showRefuseButton)

        //Сообщить что средства не получены
        val showDenyReceiptButton = dealService.denyReceipt(deal, userDetails.user)
        model.addAttribute("showDenyReceiptButton", showDenyReceiptButton)

        return "dealInfo"
    }

    @PostMapping("/add")
    fun addNewDeal(
        @RequestParam("requestId") requestId: Long,
        @RequestParam("walletId", required = false) walletId: Long?,
        @RequestParam("cardId", required = false) cardId: Long?,
        authentication: Authentication,
        redirectAttributes: RedirectAttributes
    ): String {
        var initialRequest: Request = requestService.getById(requestId)
        val userDetails = authentication.principal as MyUserDetails
        val authUserId = userDetails.user.id

        if (initialRequest.user.id == authUserId) {
//            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Невозможно создать сделку с самим собой")
            redirectAttributes.addFlashAttribute("errorMessage", "Невозможно создать сделку с самим собой")
            return "redirect:/platform/request/$requestId"
        }

        val wallet: Wallet? = if (walletId != null) walletService.getById(walletId) else null
        val user: User = userService.getById(authUserId)
        val card: Card? = if (cardId != null) cardService.getById(cardId) else null

        if (initialRequest.requestType.name == "Покупка") {
            val balance = initialRequest.quantity
            val walletBalance = wallet?.balance ?: 0.0
            if (walletBalance < balance) {
                redirectAttributes.addFlashAttribute("errorMessage", "Недостаточно вредств")
                return "redirect:/platform/request/$requestId"
            }
        }

        //Заявка контрагент
        val requestType: RequestType = if (initialRequest.requestType.name == "Продажа") {
            requestTypeService.getByName("Покупка")
        } else {
            requestTypeService.getByName("Продажа")
        }

        var newRequest = Request(
            requestType = requestType,
            wallet = wallet,
            user = user,
            card = card,
            pricePerUnit = initialRequest.pricePerUnit,
            quantity = initialRequest.quantity,
            description = "Заявка контрагент ${initialRequest.id}",
            deadlineDateTime = initialRequest.deadlineDateTime
        )

        val newStatus = "Используется в сделке"
        newRequest = requestService.add(newRequest, newStatus)
        initialRequest = requestService.updateStatusById(requestId, newStatus)

        //Создание сделки на основе заявок
        //Если исходная заявка на продажу
        val isBuyCreated = initialRequest.requestType.name == "Продажа"
        val dealStatus = dealStatusService.getByNameStatus("Подтверждение сделки")

        lateinit var sellRequest: Request
        lateinit var buyRequest: Request
        if (isBuyCreated) {
            sellRequest = initialRequest
            buyRequest = newRequest
        } else {
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

        return "redirect:/platform/deal/${resultDeal.id}?create"
    }

    //Buttons success
    @PostMapping("/{dealId}/accept")
    fun acceptDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val deal = dealService.getById(dealId)

        if (!dealService.acceptDeal(deal, userDetails.user)) {
            return "redirect:/platform/deal/${dealId}?denied"
        }

        dealService.updateStatus(dealId, "Ожидание перевода")
        return "redirect:/platform/deal/${dealId}?accept"
    }

    @PostMapping("/{dealId}/confirm_payment")
    fun confirmPaymentDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val deal = dealService.getById(dealId)

        if (!dealService.confirmPayment(deal, userDetails.user)) {
            return "redirect:/platform/deal/${dealId}?denied"
        }

        dealService.updateStatus(dealId, "Ожидание подтверждения перевода")
        return "redirect:/platform/deal/${dealId}?confirm"
    }

    @PostMapping("/{dealId}/confirm_receipt_payment")
    fun confirmReceiptPaymentDeal(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val deal = dealService.getById(dealId)

        if (!dealService.confirmReceipt(deal, userDetails.user)) {
            return "redirect:/platform/deal/${dealId}?denied"
        }

        val newStatus = "Закрыто: успешно"
        requestService.updateStatusById(deal.sellRequest.id, newStatus)
        requestService.updateStatusById(deal.buyRequest.id, newStatus)


        val walletFromId = deal.sellRequest.wallet!!.id
        val walletToId = deal.buyRequest.wallet!!.id
        val amount = deal.buyRequest.quantity

        walletService.transferBalance(walletFromId, walletToId, amount)
        dealService.updateStatus(dealId, newStatus)
        return "redirect:/platform/deal/${dealId}?confirm"
    }

    // Buttons failure
    //Отклонить предложение о создании заявки(инициатор)
    //отклонить заявку перед переводом средств(контрагент)
    @PostMapping("/{dealId}/refuse")
    fun refuseDeal(@PathVariable dealId: Long): String {
        val deal = dealService.getById(dealId)

        val newStatus = "Закрыто: неактуально"
        if (deal.isBuyCreated == true) {
            requestService.updateStatusById(deal.sellRequest.id, "Доступна на платформе")
            requestService.updateStatusById(deal.buyRequest.id, newStatus)
        } else {
            requestService.updateStatusById(deal.sellRequest.id, newStatus)
            requestService.updateStatusById(deal.buyRequest.id, "Доступна на платформе")
        }

        dealService.updateStatus(dealId, newStatus)
        return "redirect:/platform/deal/${dealId}?error"
    }

    //Если средства отправлены, но не получены
    @PostMapping("/{dealId}/deny_received_payment")
    fun denyReceived(@PathVariable dealId: Long, authentication: Authentication): String {
        val userDetails = authenticationService.getUserDetails(authentication)
        val deal = dealService.getById(dealId)

        if (!dealService.denyReceipt(deal, userDetails.user)) {
            return "redirect:/platform/deal/${dealId}?denied"
        }
        val newStatus = "Приостановлено: решение проблем"
        dealService.updateStatus(dealId, newStatus)

        return "redirect:/platform/deal/${dealId}?error"
    }

    @GetMapping("/{dealId}/document")
    fun getDealDocument(@PathVariable dealId: Long): ResponseEntity<ByteArray> {
        val deal = dealService.getById(dealId)

        val out = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(out)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        // Load DejaVu Sans font
        val fontPath = "src/main/resources/fonts/DejaVuSans.ttf"
        val font: PdfFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)

        document.setFont(font)

        // Title
        val title = Paragraph("Deal Receipt")
            .setFontSize(18f)
            .setFontColor(ColorConstants.BLUE)
            .setTextAlignment(TextAlignment.CENTER)
            .setBold()
        document.add(title)

        document.add(Paragraph(" "))

        // Date formatter
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Helper function to add a line item
        fun addLineItem(table: Table, key: String, value: String) {
            table.addCell(
                Cell().add(Paragraph(key).setFont(font).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY)
            )
            table.addCell(Cell().add(Paragraph(value).setFont(font)))
        }

        // Create a table for the details
        val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 7f))).useAllAvailableWidth()

        // Add deal data to the table
        addLineItem(table, "Deal ID", deal.id.toString())
        addLineItem(table, "Status", deal.status.name)
        addLineItem(table, "Buy Request ID", deal.buyRequest.id.toString())
        addLineItem(table, "Sell Request ID", deal.sellRequest.id.toString())
        addLineItem(table, "Created Time", dateTimeFormatter.format(deal.openDateTime))
        addLineItem(table, "Closed Time", dateTimeFormatter.format(deal.closeDateTime))
        addLineItem(table, "Buy Request User", deal.buyRequest.user.login)
        addLineItem(table, "Sell Request User", deal.sellRequest.user.login)
        addLineItem(table, "Buy Request Quantity", "${deal.buyRequest.quantity} units")
        addLineItem(table, "Sell Request Quantity", "${deal.sellRequest.quantity} units")
        addLineItem(table, "Buy Request Price per Unit", "${deal.buyRequest.pricePerUnit} currency units")
        addLineItem(table, "Sell Request Price per Unit", "${deal.sellRequest.pricePerUnit} currency units")
        addLineItem(table, "Buy Request Description", deal.buyRequest.description)
        addLineItem(table, "Sell Request Description", deal.sellRequest.description)
        addLineItem(table, "Buy Request Wallet ID", deal.buyRequest.wallet?.id?.toString() ?: "N/A")
        addLineItem(table, "Sell Request Wallet ID", deal.sellRequest.wallet?.id?.toString() ?: "N/A")
        addLineItem(table, "Buy Request Card ID", deal.buyRequest.card?.id?.toString() ?: "N/A")
        addLineItem(table, "Sell Request Card ID", deal.sellRequest.card?.id?.toString() ?: "N/A")

        document.add(table)

        // Add the total amount
        val totalAmount = Paragraph("Итоговая сумма: ${deal.sellRequest.quantity * deal.buyRequest.pricePerUnit}")
            .setFontSize(12f)
            .setFontColor(ColorConstants.RED)
            .setBold()
            .setTextAlignment(TextAlignment.RIGHT)
        document.add(totalAmount)

        // Add the report generation timestamp
        val reportCreateTime = LocalDateTime.now()
        document.add(
            Paragraph("Report generated on: ${dateTimeFormatter.format(reportCreateTime)}").setFontSize(10f)
                .setTextAlignment(TextAlignment.RIGHT)
        )

        document.close()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=deal_receipt_${deal.id}.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(out.toByteArray())
    }
}