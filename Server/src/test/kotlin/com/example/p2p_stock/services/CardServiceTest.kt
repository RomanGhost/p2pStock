package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.CardInfo
import com.example.p2p_stock.errors.DuplicateCardException
import com.example.p2p_stock.errors.NotFoundCardException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.Bank // Предполагаемый импорт
import com.example.p2p_stock.models.Card
import com.example.p2p_stock.models.User
import com.example.p2p_stock.repositories.CardRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.* // Static Mockito methods
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any // Kotlin helpers
import org.mockito.kotlin.verify // Kotlin helpers
import org.mockito.kotlin.whenever // Kotlin helpers
import org.springframework.dao.DataIntegrityViolationException
import java.util.* // For Optional

@ExtendWith(MockitoExtension::class)
class CardServiceTest {

    @Mock
    private lateinit var cardRepository: CardRepository

    @Mock
    private lateinit var bankService: BankService

    @InjectMocks
    private lateinit var cardService: CardService

    @Captor
    private lateinit var cardCaptor: ArgumentCaptor<Card>

    // Тестовые данные
    private lateinit var testUser: User
    private lateinit var testBank: Bank
    private lateinit var testCard: Card
    private lateinit var testCardInfo: CardInfo

    @BeforeEach
    fun setUp() {
        testUser = User(id = 1L, login = "testuser", password = "password", email = "test@test.com")
        // Предполагаем, что Bank имеет id и name
        testBank = Bank(name = "Test Bank")
        testCard = Card(
            id = 50L,
            cardNumber = "1234567812345678",
            cardName = "My Test Card",
            bank = testBank,
            user = testUser
        )
        testCardInfo = CardInfo(
            id = 0,
            cardNumber = "9876543210987654",
            cardName = "New Card",
            bankName = testBank.name // Используем имя банка
        )
    }

    // --- Тесты для findByUserId ---

    @Test
    fun `findByUserId should return list of cards for user`() {
        // Arrange
        val cards = listOf(testCard, testCard.copy(id = 51L, cardName = "Another Card"))
        whenever(cardRepository.findByUserId(testUser.id)).thenReturn(cards)

        // Act
        val foundCards = cardService.findByUserId(testUser.id)

        // Assert
        assertEquals(2, foundCards.size)
        assertEquals(cards, foundCards)
        verify(cardRepository).findByUserId(testUser.id)
    }

    @Test
    fun `findByUserId should return empty list when user has no cards`() {
        // Arrange
        whenever(cardRepository.findByUserId(testUser.id)).thenReturn(emptyList())

        // Act
        val foundCards = cardService.findByUserId(testUser.id)

        // Assert
        assertTrue(foundCards.isEmpty())
        verify(cardRepository).findByUserId(testUser.id)
    }

    // --- Тесты для findById ---

    @Test
    fun `findById should return card when found`() {
        // Arrange
        whenever(cardRepository.findById(testCard.id!!)).thenReturn(Optional.of(testCard))

        // Act
        val foundCard = cardService.findById(testCard.id!!)

        // Assert
        assertNotNull(foundCard)
        assertEquals(testCard.id, foundCard.id)
        verify(cardRepository).findById(testCard.id!!)
    }

    @Test
    fun `findById should throw NotFoundCardException when not found`() {
        // Arrange
        val nonExistentId = 99L
        whenever(cardRepository.findById(nonExistentId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<NotFoundCardException> {
            cardService.findById(nonExistentId)
        }
        assertEquals("Card with Id:$nonExistentId not found for User", exception.message)
        verify(cardRepository).findById(nonExistentId)
    }

    // --- Тесты для save ---

    @Test
    fun `save should call repository save and return saved card`() {
        // Arrange
        whenever(cardRepository.save(any<Card>())).thenReturn(testCard)

        // Act
        val savedCard = cardService.save(testCard)

        // Assert
        assertNotNull(savedCard)
        assertEquals(testCard.id, savedCard.id)
        verify(cardRepository).save(testCard)
    }

    // --- Тесты для addCard ---

    @Test
    fun `addCard should create and save new card successfully`() {
        // Arrange
        // 1. У пользователя нет карт с таким именем
        whenever(cardRepository.findByUserId(testUser.id)).thenReturn(emptyList())
        // 2. Сервис банков находит банк (возвращает Optional с банком)
        whenever(bankService.findById(testCardInfo.bankName)).thenReturn(Optional.of(testBank))
        // 3. Репозиторий успешно сохраняет карту
        whenever(cardRepository.save(cardCaptor.capture())).thenAnswer { invocation ->
            // Возвращаем захваченную карту, имитируя сохранение
            invocation.getArgument(0)
            // Можно добавить ID:
            // val saved = invocation.getArgument<Card>(0)
            // saved.copy(id= 1L)
        }

        // Act
        val newCard = cardService.addCard(testCardInfo, testUser)

        // Assert
        assertNotNull(newCard)
        assertEquals(testCardInfo.cardName, newCard.cardName)
        assertEquals(testCardInfo.cardNumber, newCard.cardNumber)
        assertEquals(testBank, newCard.bank)
        assertEquals(testUser, newCard.user)

        // Проверяем вызовы моков
        verify(cardRepository).findByUserId(testUser.id)
        verify(bankService).findById(testCardInfo.bankName)
        verify(cardRepository).save(any(Card::class.java))

        // Проверяем захваченную карту
        val capturedCard = cardCaptor.value
        assertEquals(testCardInfo.cardName, capturedCard.cardName)
        assertEquals(testCardInfo.cardNumber, capturedCard.cardNumber)
        assertEquals(testBank, capturedCard.bank)
        assertEquals(testUser, capturedCard.user)
        assertEquals(0L, capturedCard.id) // ID должен быть null перед сохранением
    }

    @Test
    fun `addCard should throw DuplicateCardException if card name exists for user`() {
        // Arrange: Мокаем репозиторий, чтобы он возвращал карту с таким же именем
        val existingCard = testCard.copy(cardName = testCardInfo.cardName)
        whenever(cardRepository.findByUserId(testUser.id)).thenReturn(listOf(existingCard))

        // Act & Assert
        val exception = assertThrows<DuplicateCardException> {
            cardService.addCard(testCardInfo, testUser)
        }
        assertEquals("Карта с таким названием уже существует", exception.message)

        // Убеждаемся, что дальнейшие шаги (поиск банка, сохранение) не выполнялись
        verify(cardRepository).findByUserId(testUser.id)
        verifyNoInteractions(bankService)
        verify(cardRepository, never()).save(any())
    }

    @Test
    fun `addCard should throw NoSuchElementException if bank not found`() {
        // Arrange
        whenever(cardRepository.findByUserId(testUser.id)).thenReturn(emptyList())
        // Мокаем сервис банков, чтобы он не нашел банк
        whenever(bankService.findById(testCardInfo.bankName)).thenReturn(Optional.empty())

        // Act & Assert
        // Ожидаем NoSuchElementException, так как код вызовет bank.get() на пустом Optional
        assertThrows<NoSuchElementException> {
            cardService.addCard(testCardInfo, testUser)
        }

        // Проверяем, что поиск карты и банка был, а сохранение - нет
        verify(cardRepository).findByUserId(testUser.id)
        verify(bankService).findById(testCardInfo.bankName)
        verify(cardRepository, never()).save(any())
    }

    @Test
    fun `addCard should throw DuplicateCardException on DataIntegrityViolationException during save`() {
        // Arrange
        whenever(cardRepository.findByUserId(testUser.id)).thenReturn(emptyList())
        whenever(bankService.findById(testCardInfo.bankName)).thenReturn(Optional.of(testBank))
        // Мокаем save, чтобы он выбрасывал DataIntegrityViolationException
        whenever(cardRepository.save(any(Card::class.java))).thenThrow(DataIntegrityViolationException("DB constraint violation"))

        // Act & Assert
        val exception = assertThrows<DuplicateCardException> {
            cardService.addCard(testCardInfo, testUser)
        }
        assertEquals("Карта уже существует", exception.message) // Сообщение из catch блока

        verify(cardRepository).findByUserId(testUser.id)
        verify(bankService).findById(testCardInfo.bankName)
        verify(cardRepository).save(any(Card::class.java)) // Проверяем, что попытка save была
    }

    // --- Тесты для validateOwnership ---

    @Test
    fun `validateOwnership should return card if user is owner`() {
        // Arrange
        whenever(cardRepository.findById(testCard.id!!)).thenReturn(Optional.of(testCard)) // testUser владеет testCard

        // Act
        val validatedCard = cardService.validateOwnership(testCard.id!!, testUser)

        // Assert
        assertNotNull(validatedCard)
        assertEquals(testCard.id, validatedCard.id)
        assertEquals(testUser.id, validatedCard.user?.id)
        verify(cardRepository).findById(testCard.id!!)
    }

    @Test
    fun `validateOwnership should throw OwnershipException if user is not owner`() {
        // Arrange
        val anotherUser = User(id = 2L, login = "anotheruser", password = "pwd", email = "another@test.com")
        whenever(cardRepository.findById(testCard.id!!)).thenReturn(Optional.of(testCard)) // testCard принадлежит testUser (id=1)

        // Act & Assert
        val exception = assertThrows<OwnershipException> {
            cardService.validateOwnership(testCard.id!!, anotherUser) // Валидация для user id=2
        }
        // ВАЖНО: Сообщение об ошибке в вашем коде некорректно (упоминает Wallet).
        // Тест проверяет фактическое сообщение.
        assertEquals("Wallet with id ${testCard.id!!} does not belong to user ${anotherUser.id}", exception.message)
        verify(cardRepository).findById(testCard.id!!)
    }

    @Test
    fun `validateOwnership should throw OwnershipException if card not found`() {
        // Arrange
        val nonExistentId = 99L
        whenever(cardRepository.findById(nonExistentId)).thenReturn(Optional.empty())

        // Act & Assert
        // Исключение будет выброшено, т.к. card будет null
        val exception = assertThrows<OwnershipException> {
            cardService.validateOwnership(nonExistentId, testUser)
        }
        // Опять же, сообщение некорректно
        assertEquals("Wallet with id $nonExistentId does not belong to user ${testUser.id}", exception.message)
        verify(cardRepository).findById(nonExistentId)
    }

    // --- Тесты для delete ---

    @Test
    fun `delete should call repository deleteById if user is owner`() {
        // Arrange
        whenever(cardRepository.findById(testCard.id!!)).thenReturn(Optional.of(testCard)) // testUser владеет testCard

        // Act
        cardService.delete(testCard.id!!, testUser.id)

        // Assert
        verify(cardRepository).findById(testCard.id!!)
        verify(cardRepository).deleteById(testCard.id!!)
    }

    @Test
    fun `delete should not call repository deleteById if user is not owner`() {
        // Arrange
        val anotherUserId = 2L
        whenever(cardRepository.findById(testCard.id!!)).thenReturn(Optional.of(testCard)) // testCard принадлежит testUser (id=1)

        // Act
        cardService.delete(testCard.id!!, anotherUserId) // Попытка удаления от имени user id=2

        // Assert
        verify(cardRepository).findById(testCard.id!!)
        verify(cardRepository, never()).deleteById(anyLong()) // deleteById не должен вызываться
    }

    @Test
    fun `delete should throw NoSuchElementException if card not found`() {
        // Arrange
        val nonExistentId = 99L
        whenever(cardRepository.findById(nonExistentId)).thenReturn(Optional.empty())

        // Act & Assert
        // Ожидаем исключение из-за card.get() на пустом Optional
        assertThrows<NoSuchElementException> {
            cardService.delete(nonExistentId, testUser.id)
        }

        verify(cardRepository).findById(nonExistentId)
        verify(cardRepository, never()).deleteById(anyLong())
    }
}