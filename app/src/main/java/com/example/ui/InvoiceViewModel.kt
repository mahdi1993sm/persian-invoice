package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.InvoiceWithItems
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.Product
import com.example.data.model.SellerInfo
import com.example.data.repository.InvoiceRepository
import com.example.ui.theme.ThemeColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Dashboard : Screen()
    object InvoiceList : Screen()
    data class InvoiceForm(val invoiceId: Long? = null) : Screen()
    data class InvoiceView(val invoiceId: Long) : Screen()
    object Customers : Screen()
    object Products : Screen()
    object BankAccounts : Screen()
    object SellerSettings : Screen()
    object Settings : Screen()
}

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: InvoiceRepository

    // Navigation state with backstack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Dashboard))
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InvoiceRepository(db)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        viewModelScope.launch {
            _screenStack.collect { stack ->
                _currentScreen.value = stack.lastOrNull() ?: Screen.Dashboard
            }
        }
    }

    val customers: StateFlow<List<Customer>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccount>> = repository.bankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoicesWithItems: StateFlow<List<InvoiceWithItems>> = repository.invoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sellerInfo: StateFlow<SellerInfo?> = repository.sellerInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun navigateTo(screen: Screen) {
        val current = _screenStack.value
        // If navigating to same screen type, replace or avoid duplicate
        if (current.lastOrNull() == screen) return
        _screenStack.value = current + screen
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            return true
        }
        return false
    }

    fun navigateToTab(screen: Screen) {
        _screenStack.value = listOf(screen)
    }

    // Invoice filter tab state (0: All, 1: Paid, 2: Credit, 3: Check)
    private val _invoiceFilterTab = MutableStateFlow(0)
    val invoiceFilterTab: StateFlow<Int> = _invoiceFilterTab.asStateFlow()

    fun setInvoiceFilterTab(tab: Int) {
        _invoiceFilterTab.value = tab
    }

    fun navigateToInvoiceList(tab: Int = 0) {
        _invoiceFilterTab.value = tab
        navigateTo(Screen.InvoiceList)
    }

    // App Preferences State
    private val _themeColorHex = MutableStateFlow("#10B981")
    val themeColorHex: StateFlow<String> = _themeColorHex.asStateFlow()

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _currency = MutableStateFlow("تومان")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _defaultTaxPercent = MutableStateFlow(10.0)
    val defaultTaxPercent: StateFlow<Double> = _defaultTaxPercent.asStateFlow()

    private val _invoicePrefix = MutableStateFlow("INV-1403-")
    val invoicePrefix: StateFlow<String> = _invoicePrefix.asStateFlow()

    fun setThemeColor(hex: String) {
        _themeColorHex.value = hex
        viewModelScope.launch {
            repository.setSetting("theme_color", hex)
        }
    }

    fun toggleDarkMode() {
        _darkMode.value = !_darkMode.value
        viewModelScope.launch {
            repository.setSetting("dark_mode", _darkMode.value.toString())
        }
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
        viewModelScope.launch {
            repository.setSetting("currency", newCurrency)
        }
    }

    fun setDefaultTaxPercent(tax: Double) {
        _defaultTaxPercent.value = tax
        viewModelScope.launch {
            repository.setSetting("default_tax", tax.toString())
        }
    }

    fun setInvoicePrefix(prefix: String) {
        _invoicePrefix.value = prefix
        viewModelScope.launch {
            repository.setSetting("invoice_prefix", prefix)
        }
    }

    // CRUD Customer
    fun saveCustomer(customer: Customer, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            onDone?.invoke()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // CRUD Product
    fun saveProduct(product: Product, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveProduct(product)
            onDone?.invoke()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Batch Product Operations
    fun batchAdjustPrices(
        productIds: Set<Long>,
        isPercentage: Boolean,
        amount: Double,
        isIncrease: Boolean,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val allProds = products.value
            val targetProds = allProds.filter { it.id in productIds }
            val updated = targetProds.map { prod ->
                val newPrice = if (isPercentage) {
                    val factor = if (isIncrease) (1.0 + (amount / 100.0)) else (1.0 - (amount / 100.0))
                    (prod.price * factor).toLong().coerceAtLeast(0L)
                } else {
                    val delta = amount.toLong()
                    if (isIncrease) prod.price + delta else (prod.price - delta).coerceAtLeast(0L)
                }
                prod.copy(price = newPrice)
            }
            repository.updateProducts(updated)
            onDone?.invoke()
        }
    }

    fun batchChangeCategory(productIds: Set<Long>, newCategory: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val allProds = products.value
            val targetProds = allProds.filter { it.id in productIds }
            val updated = targetProds.map { it.copy(category = newCategory) }
            repository.updateProducts(updated)
            onDone?.invoke()
        }
    }

    fun batchDeleteProducts(productIds: Set<Long>, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val allProds = products.value
            val targetProds = allProds.filter { it.id in productIds }
            repository.deleteProducts(targetProds)
            onDone?.invoke()
        }
    }

    // About Us Dialog State
    private val _showAboutUs = MutableStateFlow(false)
    val showAboutUs: StateFlow<Boolean> = _showAboutUs.asStateFlow()

    fun openAboutUs() {
        _showAboutUs.value = true
    }

    fun closeAboutUs() {
        _showAboutUs.value = false
    }

    // CRUD Bank Account
    fun saveBankAccount(bankAccount: BankAccount, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveBankAccount(bankAccount)
            onDone?.invoke()
        }
    }

    fun deleteBankAccount(bankAccount: BankAccount) {
        viewModelScope.launch {
            repository.deleteBankAccount(bankAccount)
        }
    }

    // CRUD Invoice
    fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveInvoice(invoice, items)
            onSaved(id)
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    // Seller Info
    fun saveSellerInfo(sellerInfo: SellerInfo, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveSellerInfo(sellerInfo)
            onDone?.invoke()
        }
    }

    suspend fun getInvoiceById(id: Long): InvoiceWithItems? {
        return repository.getInvoiceWithItems(id)
    }

    fun getActiveThemeColor(): Color {
        val found = ThemeColors.find { it.hex.equals(_themeColorHex.value, ignoreCase = true) }
        return found?.primary ?: ThemeColors[0].primary
    }
}
