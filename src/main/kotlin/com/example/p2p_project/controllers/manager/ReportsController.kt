package com.example.p2p_project.controllers.manager

import com.example.p2p_project.services.DealService
import com.example.p2p_project.services.RequestService
import com.example.p2p_project.services.UserService
import com.example.p2p_project.services.WalletService
import com.example.p2p_project.services.dataServices.CryptocurrencyService
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/manager/reports")
class ReportController(
    private val dealService: DealService,
    private val userService: UserService,
    private val walletService: WalletService,
    private val requestService: RequestService,
    private val cryptocurrencyService: CryptocurrencyService
) {
    @GetMapping()
    fun getAllReports(): String {
        return "reports"
    }

    @GetMapping("/deals")
    fun getDealReport(): ResponseEntity<ByteArray> {
        val deals = dealService.getAll().sortedBy { it.id }

        val out = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(out)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        // Load DejaVu Sans font
        val fontPath = "src/main/resources/fonts/DejaVuSans.ttf"
        val font: PdfFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)

        document.setFont(font)

        document.add(Paragraph("Deal Report").setFontSize(14f))

        // Create a table with 6 columns
        val table = Table(floatArrayOf(1f, 2f, 2f, 2f, 3f, 3f))

        // Headers
        val headers = arrayOf("ID", "Status", "Buy Request", "Sell Request", "Created Time", "Closed Time")
        for (header in headers) {
            table.addHeaderCell(Paragraph(header).setFont(font))
        }

        // Date formatter
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Add deal data to table
        for (deal in deals) {
            table.addCell(Paragraph(deal.id.toString()).setFont(font))
            table.addCell(Paragraph(deal.status.name).setFont(font))
            table.addCell(Paragraph(deal.buyRequest.id.toString()).setFont(font))
            table.addCell(Paragraph(deal.sellRequest.id.toString()).setFont(font))
            table.addCell(Paragraph(dateTimeFormatter.format(deal.openDateTime)).setFont(font))
            table.addCell(Paragraph(dateTimeFormatter.format(deal.closeDateTime)).setFont(font))
        }

        document.add(table)

        document.add(Paragraph("Total number of deals: ${deals.size}").setFontSize(12f))

        val crypto_balance: HashMap<String, Double> = hashMapOf()
        for (deal in deals) {
            val crypto: String = deal.sellRequest.wallet?.cryptocurrency?.name ?: continue
            crypto_balance[crypto] =
                (crypto_balance[crypto] ?: 0.0) + deal.sellRequest.quantity * deal.sellRequest.pricePerUnit
        }

        for (cb in crypto_balance) {
            document.add(Paragraph("Итоговая сумма сделок по криптовалюте - ${cb.key}: ${cb.value}").setFontSize(12f))
        }

        val reportCreateTime = LocalDateTime.now()
        document.add(Paragraph("Report generated on: ${dateTimeFormatter.format(reportCreateTime)}").setFontSize(10f))

        document.close()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=deals_report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(out.toByteArray())
    }

    @GetMapping("/users")
    fun getUserReport(): ResponseEntity<ByteArray> {
        val users = userService.getAll().sortedBy { it.id }

        val out = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(out)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        // Load DejaVu Sans font
        val fontPath = "src/main/resources/fonts/DejaVuSans.ttf"
        val font: PdfFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)

        document.setFont(font)

        document.add(Paragraph("User Report").setFontSize(14f))

        // Create a table with 3 columns
        val table = Table(floatArrayOf(1f, 3f, 2f))

        // Headers
        val headers = arrayOf("ID", "Login", "Active")
        for (header in headers) {
            table.addHeaderCell(Paragraph(header).setFont(font))
        }

        // Add user data to table
        for (user in users) {
            table.addCell(Paragraph(user.id.toString()).setFont(font))
            table.addCell(Paragraph(user.login).setFont(font))
            table.addCell(Paragraph(user.isEnabled.toString()).setFont(font))
        }

        document.add(table)

        document.add(Paragraph("Total number of users: ${users.size}").setFontSize(12f))

        val reportCreateTime = LocalDateTime.now()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        document.add(Paragraph("Report generated on: ${dateTimeFormatter.format(reportCreateTime)}").setFontSize(10f))

        document.close()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(out.toByteArray())
    }

    @GetMapping("/wallets")
    fun getWalletReport(): ResponseEntity<ByteArray> {
        val wallets = walletService.getAll().sortedBy { it.id }

        val out = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(out)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        // Load DejaVu Sans font
        val fontPath = "src/main/resources/fonts/DejaVuSans.ttf"
        val font: PdfFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)

        document.setFont(font)

        document.add(Paragraph("Wallet Report").setFontSize(14f))

        // Create a table with 5 columns
        val table = Table(floatArrayOf(1f, 2f, 2f, 2f, 2f))

        // Headers
        val headers = arrayOf("ID", "User ID", "Cryptocurrency", "Balance", "Name")
        for (header in headers) {
            table.addHeaderCell(Paragraph(header).setFont(font))
        }

        // Add wallet data to table
        for (wallet in wallets) {
            table.addCell(Paragraph(wallet.id.toString()).setFont(font))
            table.addCell(Paragraph(wallet.user.id.toString()).setFont(font))
            table.addCell(Paragraph(wallet.cryptocurrency.id.toString()).setFont(font))
            table.addCell(Paragraph(wallet.balance.toString()).setFont(font))
            table.addCell(Paragraph(wallet.name).setFont(font))
        }

        document.add(table)

        document.add(Paragraph("Total number of wallets: ${wallets.size}").setFontSize(12f))

        val reportCreateTime = LocalDateTime.now()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        document.add(Paragraph("Report generated on: ${dateTimeFormatter.format(reportCreateTime)}").setFontSize(10f))

        document.close()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=wallets_report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(out.toByteArray())
    }

    @GetMapping("/requests")
    fun getRequestReport(): ResponseEntity<ByteArray> {
        val requests = requestService.getAll().sortedBy { it.id }

        val out = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(out)
        val pdfDocument = PdfDocument(pdfWriter)

        // Set page to landscape
        pdfDocument.defaultPageSize = PageSize.A4.rotate()

        val document = Document(pdfDocument)

        // Load DejaVu Sans font
        val fontPath = "src/main/resources/fonts/DejaVuSans.ttf"
        val font: PdfFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)

        document.setFont(font)

        document.add(Paragraph("Request Report").setFontSize(14f))

        // Create a table with 12 columns
        val table = Table(floatArrayOf(1f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 3f, 3f, 3f, 3f))

        // Headers
        val headers = arrayOf(
            "ID",
            "Type",
            "Wallet ID",
            "User ID",
            "Card ID",
            "Status",
            "Manager ID",
            "Price/Unit",
            "Quantity",
            "Description",
            "Created Time",
            "Deadline Time"
        )
        for (header in headers) {
            table.addHeaderCell(Cell().add(Paragraph(header).setFont(font)))
        }

        // Date formatter
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Add request data to table
        for (request in requests) {
            table.addCell(Cell().add(Paragraph(request.id.toString()).setFont(font)))
            table.addCell(Cell().add(Paragraph(request.requestType.name).setFont(font)))
            table.addCell(Cell().add(Paragraph(request.wallet?.id?.toString() ?: "").setFont(font)))
            table.addCell(Cell().add(Paragraph(request.user.id.toString()).setFont(font)))
            table.addCell(Cell().add(Paragraph(request.card?.id?.toString() ?: "").setFont(font)))
            table.addCell(Cell().add(Paragraph(request.requestStatus.name).setFont(font)))
            table.addCell(Cell().add(Paragraph(request.managerId?.id?.toString() ?: "").setFont(font)))
            table.addCell(Cell().add(Paragraph(request.pricePerUnit.toString()).setFont(font)))
            table.addCell(Cell().add(Paragraph(request.quantity.toString()).setFont(font)))
            table.addCell(Cell().add(Paragraph(request.description).setFont(font)))
            table.addCell(Cell().add(Paragraph(dateTimeFormatter.format(request.createDateTime)).setFont(font)))
            table.addCell(Cell().add(Paragraph(dateTimeFormatter.format(request.deadlineDateTime)).setFont(font)))
        }

        document.add(table)

        document.add(Paragraph("Total number of requests: ${requests.size}").setFontSize(12f))

        val reportCreateTime = LocalDateTime.now()
        document.add(Paragraph("Report generated on: ${dateTimeFormatter.format(reportCreateTime)}").setFontSize(10f))

        document.close()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=requests_report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(out.toByteArray())
    }
}
