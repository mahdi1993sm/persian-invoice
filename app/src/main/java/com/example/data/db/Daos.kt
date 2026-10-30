package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AppSetting
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.Product
import com.example.data.model.SellerInfo
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
)

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY id DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCount(): Int
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Update
    suspend fun updateAll(products: List<Product>)

    @Delete
    suspend fun delete(product: Product)

    @Delete
    suspend fun deleteAll(products: List<Product>)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts ORDER BY id ASC")
    fun getAllBankAccounts(): Flow<List<BankAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bankAccount: BankAccount): Long

    @Update
    suspend fun update(bankAccount: BankAccount)

    @Delete
    suspend fun delete(bankAccount: BankAccount)

    @Query("SELECT COUNT(*) FROM bank_accounts")
    suspend fun getCount(): Int
}

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)

    @Transaction
    suspend fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = if (invoice.id == 0L) {
            insertInvoice(invoice)
        } else {
            updateInvoice(invoice)
            deleteItemsForInvoice(invoice.id)
            invoice.id
        }
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        insertItems(itemsWithId)
        return invoiceId
    }

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getCount(): Int
}

@Dao
interface SellerInfoDao {
    @Query("SELECT * FROM seller_info WHERE id = 1 LIMIT 1")
    fun getSellerInfo(): Flow<SellerInfo?>

    @Query("SELECT * FROM seller_info WHERE id = 1 LIMIT 1")
    suspend fun getSellerInfoOnce(): SellerInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(sellerInfo: SellerInfo)
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSetting>>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSetting)
}
