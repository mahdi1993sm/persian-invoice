package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.InvoiceWithItems
import com.example.data.model.AppSetting
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.Product
import com.example.data.model.SellerInfo
import com.example.utils.JalaliCalendar
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val database: AppDatabase) {

    val customers: Flow<List<Customer>> = database.customerDao().getAllCustomers()
    val products: Flow<List<Product>> = database.productDao().getAllProducts()
    val bankAccounts: Flow<List<BankAccount>> = database.bankAccountDao().getAllBankAccounts()
    val invoicesWithItems: Flow<List<InvoiceWithItems>> = database.invoiceDao().getAllInvoicesWithItems()
    val sellerInfo: Flow<SellerInfo?> = database.sellerInfoDao().getSellerInfo()
    val settings: Flow<List<AppSetting>> = database.settingDao().getAllSettings()

    suspend fun getCustomer(id: Long) = database.customerDao().getCustomerById(id)
    suspend fun saveCustomer(customer: Customer) = database.customerDao().insert(customer)
    suspend fun deleteCustomer(customer: Customer) = database.customerDao().delete(customer)

    suspend fun getProduct(id: Long) = database.productDao().getProductById(id)
    suspend fun saveProduct(product: Product) = database.productDao().insert(product)
    suspend fun deleteProduct(product: Product) = database.productDao().delete(product)
    suspend fun updateProducts(products: List<Product>) = database.productDao().updateAll(products)
    suspend fun deleteProducts(products: List<Product>) = database.productDao().deleteAll(products)

    suspend fun saveBankAccount(bankAccount: BankAccount) = database.bankAccountDao().insert(bankAccount)
    suspend fun deleteBankAccount(bankAccount: BankAccount) = database.bankAccountDao().delete(bankAccount)

    suspend fun getInvoiceWithItems(id: Long) = database.invoiceDao().getInvoiceWithItemsById(id)
    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        return database.invoiceDao().saveInvoiceWithItems(invoice, items)
    }
    suspend fun deleteInvoice(invoice: Invoice) = database.invoiceDao().deleteInvoice(invoice)

    suspend fun saveSellerInfo(sellerInfo: SellerInfo) = database.sellerInfoDao().insertOrUpdate(sellerInfo)

    suspend fun setSetting(key: String, value: String) {
        database.settingDao().setSetting(AppSetting(key, value))
    }

    suspend fun getSetting(key: String): String? {
        return database.settingDao().getSetting(key)
    }

    suspend fun seedInitialDataIfEmpty() {
        // Initialize default application settings only (leaving customers, products, invoices, and bank accounts empty)
        if (getSetting("theme_color") == null) {
            setSetting("theme_color", "#10B981")
        }
        if (getSetting("currency") == null) {
            setSetting("currency", "تومان")
        }
        if (getSetting("default_tax") == null) {
            setSetting("default_tax", "10")
        }
        if (getSetting("invoice_prefix") == null) {
            setSetting("invoice_prefix", "INV-1403-")
        }
        if (getSetting("theme_mode") == null) {
            setSetting("theme_mode", "SYSTEM")
        }
    }

    suspend fun clearAllData() {
        database.clearAllTables()
        seedInitialDataIfEmpty()
    }
}
