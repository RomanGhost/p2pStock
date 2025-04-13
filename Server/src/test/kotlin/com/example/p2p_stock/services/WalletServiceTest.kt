package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.WalletInfo
import com.example.p2p_stock.errors.DuplicateWalletException
import com.example.p2p_stock.errors.NotFoundWalletException
import com.example.p2p_stock.errors.OwnershipException
import com.example.p2p_stock.models.Cryptocurrency
import com.example.p2p_stock.dataclasses.sender.KeyData
import com.example.p2p_stock.models.User
import com.example.p2p_stock.models.Wallet
import com.example.p2p_stock.repositories.WalletRepository
import com.example.p2p_stock.services.sender.ApiService
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
import org.mockito.Mockito.* // Import Mockito static methods
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any // Specific Kotlin helper from mockito-kotlin
import org.mockito.kotlin.verify // Specific Kotlin helper
import org.mockito.kotlin.whenever // Specific Kotlin helper
import org.springframework.dao.DataIntegrityViolationException
import java.util.* // For Optional

@ExtendWith(MockitoExtension::class) // Включаем расширение Mockito для JUnit 5
class WalletServiceTest {

    // Мокаем зависимости
    @Mock
    private lateinit var walletRepository: WalletRepository

    @Mock
    private lateinit var cryptocurrencyService: CryptocurrencyService

    @Mock
    private lateinit var senderApiService: ApiService

    // Внедряем моки в тестируемый сервис
    @InjectMocks
    private lateinit var walletService: WalletService

    // ArgumentCaptor для захвата аргументов при вызове моков
    @Captor
    private lateinit var walletCaptor: ArgumentCaptor<Wallet>

    // Тестовые данные
    private lateinit var testUser: User
    private lateinit var testWallet: Wallet
    private lateinit var testCrypto: Cryptocurrency
    private lateinit var testWalletInfo: WalletInfo
    private lateinit var testKeyData: KeyData

    @BeforeEach // Метод, выполняемый перед каждым тестом
    fun setUp() {
        // Инициализация общих тестовых данных
        testUser = User(id = 1L, login = "testuser", password = "password", email = "test@test.com")
        testCrypto = Cryptocurrency(code = "BTC", name = "Bitcoin")
        testWallet = Wallet(
            id = 10L,
            cryptocurrency = testCrypto,
            balance = 1.0,
            name = "My BTC Wallet",
            user = testUser,
            publicKey = "pubKey123",
            privateKey = "privKey456"
        )
        testWalletInfo = WalletInfo(0, "New BTC Wallet", 10.0,"BTC")
        testKeyData = KeyData(publicKey = "newPubKey789", privateKey = "newPrivKey101") // Замените KeyData на реальный класс, если он другой
    }

    // --- Тесты для findById ---

    @Test
    fun `findById should return wallet when found`() {
        // Arrange: Настраиваем мок - когда findById вызывается с ID кошелька, вернуть Optional с кошельком
        whenever(walletRepository.findById(testWallet.id)).thenReturn(Optional.of(testWallet))

        // Act: Вызываем тестируемый метод
        val foundWallet = walletService.findById(testWallet.id)

        // Assert: Проверяем, что найденный кошелек соответствует ожидаемому
        assertNotNull(foundWallet)
        assertEquals(testWallet.id, foundWallet.id)
        assertEquals(testWallet.name, foundWallet.name)
        verify(walletRepository).findById(testWallet.id) // Убеждаемся, что метод репозитория был вызван
    }

    @Test
    fun `findById should throw NotFoundWalletException when not found`() {
        // Arrange: Настраиваем мок - когда findById вызывается с любым Long, вернуть пустой Optional
        val nonExistentId = 99L
        whenever(walletRepository.findById(nonExistentId)).thenReturn(Optional.empty())

        // Act & Assert: Проверяем, что при вызове метода выбрасывается ожидаемое исключение
        val exception = assertThrows<NotFoundWalletException> {
            walletService.findById(nonExistentId)
        }
        assertEquals("Wallet with Id:$nonExistentId not found", exception.message)
        verify(walletRepository).findById(nonExistentId)
    }

    // --- Тесты для findByPublicKey ---

    @Test
    fun `findByPublicKey should return wallet when found`() {
        // Arrange
        whenever(walletRepository.findByPublicKey(testWallet.publicKey!!)).thenReturn(testWallet)

        // Act
        val foundWallet = walletService.findByPublicKey(testWallet.publicKey!!)

        // Assert
        assertNotNull(foundWallet)
        assertEquals(testWallet.publicKey, foundWallet?.publicKey)
        verify(walletRepository).findByPublicKey(testWallet.publicKey!!)
    }

    @Test
    fun `findByPublicKey should return null when not found`() {
        // Arrange
        val nonExistentKey = "nonExistentKey"
        whenever(walletRepository.findByPublicKey(nonExistentKey)).thenReturn(null)

        // Act
        val foundWallet = walletService.findByPublicKey(nonExistentKey)

        // Assert
        assertNull(foundWallet)
        verify(walletRepository).findByPublicKey(nonExistentKey)
    }


    // --- Тесты для findByUserId ---

    @Test
    fun `findByUserId should return list of wallets for user`() {
        // Arrange
        val wallets = listOf(testWallet, testWallet.copy(id = 11L, name = "Another Wallet"))
        whenever(walletRepository.findByUserId(testUser.id)).thenReturn(wallets)

        // Act
        val foundWallets = walletService.findByUserId(testUser.id)

        // Assert
        assertEquals(2, foundWallets.size)
        assertEquals(wallets, foundWallets)
        verify(walletRepository).findByUserId(testUser.id)
    }

    @Test
    fun `findByUserId should return empty list when user has no wallets`() {
        // Arrange
        whenever(walletRepository.findByUserId(testUser.id)).thenReturn(emptyList())

        // Act
        val foundWallets = walletService.findByUserId(testUser.id)

        // Assert
        assertTrue(foundWallets.isEmpty())
        verify(walletRepository).findByUserId(testUser.id)
    }

    // --- Тесты для save ---

    @Test
    fun `save should call repository save and return saved wallet`() {
        // Arrange
        whenever(walletRepository.save(any<Wallet>())).thenReturn(testWallet) // Используем any() или передаем точный объект

        // Act
        val savedWallet = walletService.save(testWallet)

        // Assert
        assertNotNull(savedWallet)
        assertEquals(testWallet.id, savedWallet.id)
        verify(walletRepository).save(testWallet) // Проверяем вызов save с нашим кошельком
    }

    // --- Тесты для addWallet ---

    @Test
    fun `addWallet should create and save new wallet successfully`() {
        // Arrange
        // 1. У пользователя нет кошельков с таким именем
        whenever(walletRepository.findByUserId(testUser.id)).thenReturn(emptyList())
        // 2. Сервис криптовалют находит криптовалюту
        whenever(cryptocurrencyService.findByCode(testWalletInfo.cryptocurrencyCode)).thenReturn(testCrypto)
        // 3. Сервис генерации ключей возвращает ключи
        whenever(senderApiService.generateKeys()).thenReturn(testKeyData)
        // 4. Репозиторий успешно сохраняет кошелек
        // Используем ArgumentCaptor для проверки переданного в save объекта
        whenever(walletRepository.save(walletCaptor.capture())).thenAnswer { invocation ->
            invocation.getArgument(0) // Возвращаем захваченный кошелек как результат save
        }


        // Act
        val newWallet = walletService.addWallet(testWalletInfo, testUser)

        // Assert
        // Проверяем возвращенный кошелек
        assertNotNull(newWallet)
        assertEquals(testWalletInfo.walletName, newWallet.name)
        assertEquals(0.0, newWallet.balance)
        assertEquals(testCrypto, newWallet.cryptocurrency)
        assertEquals(testUser, newWallet.user)
        assertEquals(testKeyData.publicKey, newWallet.publicKey)
        assertEquals(testKeyData.privateKey, newWallet.privateKey)

        // Проверяем вызовы моков
        verify(walletRepository).findByUserId(testUser.id)
        verify(cryptocurrencyService).findByCode(testWalletInfo.cryptocurrencyCode)
        verify(senderApiService).generateKeys()
        verify(walletRepository).save(any(Wallet::class.java)) // Убеждаемся, что save был вызван

        // Проверяем захваченный кошелек, переданный в save
        val capturedWallet = walletCaptor.value
        assertEquals(testWalletInfo.walletName, capturedWallet.name)
        assertEquals(0.0, capturedWallet.balance)
        assertEquals(testCrypto, capturedWallet.cryptocurrency)
        assertEquals(testUser, capturedWallet.user)
        assertEquals(testKeyData.publicKey, capturedWallet.publicKey)
        assertEquals(testKeyData.privateKey, capturedWallet.privateKey)
        assertEquals(0L, capturedWallet.id)
    }

    @Test
    fun `addWallet should throw DuplicateWalletException if wallet name exists for user`() {
        // Arrange: Мокаем репозиторий, чтобы он возвращал кошелек с таким же именем
        val existingWallet = testWallet.copy(name = testWalletInfo.walletName) // Кошелек с таким же именем, как в info
        whenever(walletRepository.findByUserId(testUser.id)).thenReturn(listOf(existingWallet))

        // Act & Assert: Проверяем выброс исключения
        val exception = assertThrows<DuplicateWalletException> {
            walletService.addWallet(testWalletInfo, testUser)
        }
        assertEquals("Кошелек с таким названием уже существует", exception.message)

        // Убеждаемся, что дальнейшие шаги (поиск крипты, генерация ключей, сохранение) не выполнялись
        verify(walletRepository).findByUserId(testUser.id)
        verifyNoInteractions(cryptocurrencyService)
        verifyNoInteractions(senderApiService)
        verify(walletRepository, never()).save(any())
    }

    @Test
    fun `addWallet should throw DuplicateWalletException on DataIntegrityViolationException during save`() {
        // Arrange
        whenever(walletRepository.findByUserId(testUser.id)).thenReturn(emptyList())
        whenever(cryptocurrencyService.findByCode(testWalletInfo.cryptocurrencyCode)).thenReturn(testCrypto)
        whenever(senderApiService.generateKeys()).thenReturn(testKeyData)
        // Мокаем save, чтобы он выбрасывал DataIntegrityViolationException
        whenever(walletRepository.save(any(Wallet::class.java))).thenThrow(DataIntegrityViolationException("DB constraint violation"))

        // Act & Assert
        val exception = assertThrows<DuplicateWalletException> {
            walletService.addWallet(testWalletInfo, testUser)
        }
        assertEquals("Кошелек уже существует", exception.message) // Сообщение из catch блока

        verify(walletRepository).findByUserId(testUser.id)
        verify(cryptocurrencyService).findByCode(testWalletInfo.cryptocurrencyCode)
        verify(senderApiService).generateKeys()
        verify(walletRepository).save(any(Wallet::class.java)) // Проверяем, что попытка save была
    }


    // --- Тесты для validateOwnership ---

    @Test
    fun `validateOwnership should return wallet if user is owner`() {
        // Arrange
        whenever(walletRepository.findById(testWallet.id)).thenReturn(Optional.of(testWallet)) // Пользователь testUser владеет testWallet

        // Act
        val validatedWallet = walletService.validateOwnership(testWallet.id, testUser)

        // Assert
        assertNotNull(validatedWallet)
        assertEquals(testWallet.id, validatedWallet.id)
        assertEquals(testUser.id, validatedWallet.user?.id)
        verify(walletRepository).findById(testWallet.id)
    }

    @Test
    fun `validateOwnership should throw OwnershipException if user is not owner`() {
        // Arrange
        val anotherUser = User(id = 2L, "anotheruser", "pwd", "another@test.com")
        whenever(walletRepository.findById(testWallet.id)).thenReturn(Optional.of(testWallet)) // testWallet принадлежит testUser (id=1)

        // Act & Assert
        val exception = assertThrows<OwnershipException> {
            walletService.validateOwnership(testWallet.id, anotherUser) // Пытаемся валидировать для другого юзера (id=2)
        }
        assertEquals("Wallet with id ${testWallet.id} does not belong to user ${anotherUser.id}", exception.message)
        verify(walletRepository).findById(testWallet.id)
    }

    @Test
    fun `validateOwnership should throw OwnershipException if wallet not found`() {
        // Arrange
        val nonExistentId = 99L
        whenever(walletRepository.findById(nonExistentId)).thenReturn(Optional.empty())

        // Act & Assert
        // Исключение OwnershipException будет выброшено, так как wallet будет null,
        // и условие `null?.user?.id != user.id` сработает как true
        val exception = assertThrows<OwnershipException> {
            walletService.validateOwnership(nonExistentId, testUser)
        }
        assertEquals("Wallet with id $nonExistentId does not belong to user ${testUser.id}", exception.message)

        verify(walletRepository).findById(nonExistentId)
    }

    // --- Тесты для delete ---

    @Test
    fun `delete should call repository deleteById if user is owner`() {
        // Arrange
        whenever(walletRepository.findById(testWallet.id)).thenReturn(Optional.of(testWallet)) // Пользователь testUser владеет testWallet

        // Act
        walletService.delete(testWallet.id, testUser.id)

        // Assert
        verify(walletRepository).findById(testWallet.id) // Проверяем поиск
        verify(walletRepository).deleteById(testWallet.id) // Проверяем вызов удаления
    }

    @Test
    fun `delete should not call repository deleteById if user is not owner`() {
        // Arrange
        val anotherUserId = 2L
        whenever(walletRepository.findById(testWallet.id)).thenReturn(Optional.of(testWallet)) // testWallet принадлежит testUser (id=1)

        // Act
        walletService.delete(testWallet.id, anotherUserId) // Пытаемся удалить от имени другого пользователя (id=2)

        // Assert
        verify(walletRepository).findById(testWallet.id) // Проверяем поиск
        verify(walletRepository, never()).deleteById(anyLong()) // Убеждаемся, что deleteById НЕ вызывался
    }

    @Test
    fun `delete should throw NoSuchElementException if wallet not found`() {
        // Arrange
        val nonExistentId = 99L
        whenever(walletRepository.findById(nonExistentId)).thenReturn(Optional.empty())

        // Act & Assert
        // В текущей реализации метод delete вызовет wallet.get() на пустом Optional, что приведет к NoSuchElementException
        assertThrows<NoSuchElementException> {
            walletService.delete(nonExistentId, testUser.id)
        }

        verify(walletRepository).findById(nonExistentId) // Проверяем поиск
        verify(walletRepository, never()).deleteById(anyLong()) // Убеждаемся, что deleteById НЕ вызывался
    }
}