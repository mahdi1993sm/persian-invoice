package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val mobile: String = "",
    val address: String = "",
    val postalCode: String = "",
    val province: String = "",
    val city: String = "",
    val nationalCode: String = "",
    val economicCode: String = "",
    val createdAt: String = ""
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val unit: String = "عدد",
    val price: Long = 0L,
    val category: String = "عمومی",
    val description: String = ""
)

@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val accountNumber: String = "",
    val cardNumber: String = "",
    val shebaNumber: String = "",
    val ownerName: String = "",
    val colorIndex: Int = 0 // for card gradient styling
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long,
    val customerName: String,
    val createdAt: String,       // Jalali: "1403/08/15"
    val dueDate: String = "",    // Jalali: "1403/09/15"
    val paymentType: String = "cash", // "cash", "credit", "check"
    val cashPaymentMethod: String = "pos", // "pos", "card_to_card", "paya", "satna", "cash"
    val status: String = "paid", // "paid" (تسویه شده), "credit" (نسیه), "partial" (جزئی)
    val subtotal: Long = 0L,
    val discountPercent: Double = 0.0,
    val discountAmount: Long = 0L,
    val taxPercent: Double = 0.0,
    val taxAmount: Long = 0L,
    val totalAmount: Long = 0L,
    val paidAmount: Long = 0L,
    val remainingAmount: Long = 0L,
    val bankAccountId: Long? = null,
    val bankAccountName: String = "",
    val description: String = "",
    // Financial payment details
    val paymentDate: String = "",       // Jalali date of payment: "1403/08/15"
    val paymentMethod: String = "",     // "کارت به کارت", "دستگاه کارتخوان (پوز)", "واریز به حساب / ساتنا", "نقدی", "چک صیادی"
    val paymentAccountInfo: String = "",// Destination account or card details
    val paymentTrackingCode: String = "",// Tracking / reference code
    // Check info if paymentType == "check"
    val checkBankName: String = "",
    val checkNumber: String = "",
    val checkSayadiId: String = "",
    val checkDueDate: String = "",
    val checkIssuer: String = "",
    val checkAmount: Long = 0L
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long? = null,
    val productName: String,
    val quantity: Double = 1.0,
    val unit: String = "عدد",
    val unitPrice: Long = 0L,
    val discountPercent: Double = 0.0,
    val totalRowPrice: Long = 0L
)

@Entity(tableName = "seller_info")
data class SellerInfo(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "فروشگاه نمونه",
    val phone: String = "۰۲۱-۸۸۸۸۸۸۸۸",
    val mobile: String = "۰۹۱۲۳۴۵۶۷۸۹",
    val address: String = "تهران، خیابان ولیعصر، پلاک ۱۰۰",
    val postalCode: String = "۱۹۸۷۶۵۴۳۲۱",
    val province: String = "تهران",
    val city: String = "تهران",
    val nationalCode: String = "۱۰۱۰۱۰۱۰۱۰۱",
    val economicCode: String = "۴۱۱۱۱۱۱۱۱۱",
    val signatureName: String = "مدیریت فروشگاه",
    val signatureImagePath: String = "",
    val stampImagePath: String = "",
    val footerText: String = "کالای فروخته شده در صورت سلامت فیزیکی تا ۷ روز قابل تعویض می‌باشد. از خرید و اعتماد شما سپاسگزاریم."
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val key: String,
    val value: String
)
