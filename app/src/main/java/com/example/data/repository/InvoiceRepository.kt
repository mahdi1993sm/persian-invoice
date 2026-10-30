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
        val customerCount = database.customerDao().getCount()
        if (customerCount > 0) return

        // Seed Seller Info
        database.sellerInfoDao().insertOrUpdate(
            SellerInfo(
                id = 1,
                name = "بازرگانی پیشرو پارس",
                phone = "۰۲۱-۸۸۹۹۰۰۱۱",
                mobile = "۰۹۱۲۳۴۵۶۷۸۹",
                address = "تهران، خیابان ولیعصر، تقاطع فاطمی، پلاک ۱۱۰، واحد ۵",
                postalCode = "۱۴۳۱۶۷۸۹۵۴",
                province = "تهران",
                city = "تهران",
                nationalCode = "۱۰۱۰۳۴۵۶۷۸۹",
                economicCode = "۴۱۱۵۶۷۸۹۰۱۲۳",
                signatureName = "مهدی صفری (مدیریت فروش)",
                footerText = "کالاهای تحویل شده دارای ۱۲ ماه گارانتی تعویض بوده و تا ۷ روز پس از تاریخ تحویل قابل عودت می‌باشد."
            )
        )

        // Seed Default Settings
        setSetting("theme_color", "#10B981")
        setSetting("currency", "تومان")
        setSetting("default_tax", "10")
        setSetting("invoice_prefix", "INV-1403-")

        // Seed Customers
        val c1Id = database.customerDao().insert(
            Customer(
                name = "شرکت داده‌پردازان نوآوران مهر",
                phone = "۰۲۱-۲۲۳۳۴۴۵۵",
                mobile = "۰۹۱۲۱۱۱۱۱۱۱",
                address = "تهران، سعادت آباد، میدان کاج، خیابان سرو غربی، پلاک ۲۴",
                postalCode = "۱۹۹۸۷۶۵۴۳۲",
                province = "تهران",
                city = "تهران",
                nationalCode = "۱۰۳۲۰۱۱۱۱۱۱",
                economicCode = "۴۱۱۳۳۳۳۳۳۳",
                createdAt = "1403/05/10"
            )
        )

        val c2Id = database.customerDao().insert(
            Customer(
                name = "مهندس علیرضا رضایی",
                phone = "۰۳۱-۳۲۲۲۴۴۵۵",
                mobile = "۰۹۱۳۲۲۲۳۳۴۴",
                address = "اصفهان، خیابان چهارباغ بالا، مجتمع اداری کوثر، طبقه ۲",
                postalCode = "۸۱۶۵۴۳۲۱۰۹",
                province = "اصفهان",
                city = "اصفهان",
                nationalCode = "۱۲۸۹۹۹۸۸۷۷",
                economicCode = "",
                createdAt = "1403/06/01"
            )
        )

        val c3Id = database.customerDao().insert(
            Customer(
                name = "فروشگاه نگین کویر یزد",
                phone = "۰۳۵-۳۶۲۲۱۱۰۰",
                mobile = "۰۹۱۳۵۵۵۶۶۷۷",
                address = "یزد، میدان شهید بهشتی، خیابان فرخی، پلاک ۸۵",
                postalCode = "۸۹۱۳۵۷۹۲۴۶",
                province = "یزد",
                city = "یزد",
                nationalCode = "۴۴۳۰۱۵۶۷۸۹",
                economicCode = "۴۱۱۸۸۷۷۶۶۵",
                createdAt = "1403/07/15"
            )
        )

        val c4Id = database.customerDao().insert(
            Customer(
                name = "خانم سارا کریمی (فریلنسر)",
                phone = "۰۷۱-۳۶۲۸۰۰۰۰",
                mobile = "۰۹۱۷۳۳۳۴۴۵۵",
                address = "شیراز، بلوار ارم، کوچه ۱۲، پلاک ۴",
                postalCode = "۷۱۴۵۶۹۸۷۱۲",
                province = "فارس",
                city = "شیراز",
                nationalCode = "۲۲۸۰۱۲۳۴۵۶",
                economicCode = "",
                createdAt = "1403/08/01"
            )
        )

        // Seed Products
        val p1 = database.productDao().insert(
            Product(name = "لپ‌تاپ مهندسی Asus Vivobook 16", code = "PRD-101", unit = "دستگاه", price = 42500000L, category = "سخت‌افزار", description = "پردازنده Core i7، رم ۱۶ گیگ، حافظه ۵۱۲ SSD")
        )
        val p2 = database.productDao().insert(
            Product(name = "مانیتور ۲۷ اینچ گیمینگ سامسونگ", code = "PRD-102", unit = "دستگاه", price = 11800000L, category = "نمایشگر", description = "رزولوشن 2K، نرخ نوسازی ۱۴۴ هرتز")
        )
        val p3 = database.productDao().insert(
            Product(name = "کیبورد و ماوس بی‌سیم لاجیتک MK270", code = "PRD-103", unit = "بسته", price = 1650000L, category = "لوازم جانبی", description = "اتصال وایرلس ۲.۴ گیگاهرتز")
        )
        val p4 = database.productDao().insert(
            Product(name = "پرینتر لیزری چندکاره اچ‌پی M135w", code = "PRD-104", unit = "دستگاه", price = 19400000L, category = "ماشین‌های اداری", description = "چاپ مشکی با وای‌فای و اسکنر")
        )
        val p5 = database.productDao().insert(
            Product(name = "کابل شبکه نگزنس Cat6 تمام مس", code = "PRD-105", unit = "حلقه ۳۰۵متر", price = 4200000L, category = "تجهیزات شبکه", description = "روکش LSZH ضد حریق")
        )
        val p6 = database.productDao().insert(
            Product(name = "کاغذ A4 کپی مکس ۸۰ گرمی", code = "PRD-106", unit = "کارتن ۵تایی", price = 1350000L, category = "ملزومات مصرفی", description = "برش لیزری با کیفیت ممتاز")
        )

        // Seed Bank Accounts
        val b1 = database.bankAccountDao().insert(
            BankAccount(
                bankName = "بانک ملت",
                accountNumber = "۸۸۵۴۳۲۱۰۹۸",
                cardNumber = "۶۱۰۴-۳۳۷۸-۹۰۱۲-۳۴۵۶",
                shebaNumber = "IR120120000000001234567890",
                ownerName = "بازرگانی پیشرو پارس",
                colorIndex = 0
            )
        )
        val b2 = database.bankAccountDao().insert(
            BankAccount(
                bankName = "بانک سامان (بلوبانک)",
                accountNumber = "۶۵۴۳۲۱۷۸۹",
                cardNumber = "۶۲۱۹-۸۶۱۰-۵۵۴۴-۳۳۲۲",
                shebaNumber = "IR780560000000003344556677",
                ownerName = "مهدی صفری",
                colorIndex = 1
            )
        )
        val b3 = database.bankAccountDao().insert(
            BankAccount(
                bankName = "بانک ملی ایران",
                accountNumber = "۰۱۰۲۳۴۵۶۷۸۰۰۲",
                cardNumber = "۶۰۳۷-۹۹۱۸-۲۳۴۵-۶۷۸۹",
                shebaNumber = "IR340170000000009876543210",
                ownerName = "بازرگانی پیشرو پارس",
                colorIndex = 2
            )
        )

        // Seed Invoices
        // Invoice 1: Paid (Cash)
        val inv1 = Invoice(
            invoiceNumber = "INV-1403-101",
            customerId = c1Id,
            customerName = "شرکت داده‌پردازان نوآوران مهر",
            createdAt = "1403/07/20",
            dueDate = "1403/07/20",
            paymentType = "cash",
            cashPaymentMethod = "paya",
            status = "paid",
            subtotal = 96800000L,
            discountPercent = 5.0,
            discountAmount = 4840000L,
            taxPercent = 10.0,
            taxAmount = 9196000L,
            totalAmount = 101156000L,
            paidAmount = 101156000L,
            remainingAmount = 0L,
            bankAccountId = b1,
            bankAccountName = "بانک ملت",
            description = "تحویل در محل دفتر خریدار با تست کامل قطعات"
        )
        val inv1Items = listOf(
            InvoiceItem(productName = "لپ‌تاپ مهندسی Asus Vivobook 16", quantity = 2.0, unit = "دستگاه", unitPrice = 42500000L, discountPercent = 0.0, totalRowPrice = 85000000L),
            InvoiceItem(productName = "مانیتور ۲۷ اینچ گیمینگ سامسونگ", quantity = 1.0, unit = "دستگاه", unitPrice = 11800000L, discountPercent = 0.0, totalRowPrice = 11800000L)
        )
        database.invoiceDao().saveInvoiceWithItems(inv1, inv1Items)

        // Invoice 2: Credit (نسیه)
        val inv2 = Invoice(
            invoiceNumber = "INV-1403-102",
            customerId = c2Id,
            customerName = "مهندس علیرضا رضایی",
            createdAt = "1403/08/02",
            dueDate = "1403/09/02",
            paymentType = "credit",
            status = "credit",
            subtotal = 27800000L,
            discountPercent = 0.0,
            discountAmount = 0L,
            taxPercent = 10.0,
            taxAmount = 2780000L,
            totalAmount = 30580000L,
            paidAmount = 5000000L,
            remainingAmount = 25580000L,
            bankAccountId = b2,
            bankAccountName = "بانک سامان (بلوبانک)",
            description = "پیش‌پرداخت ۵ میلیون تومان نقدی، مانده تسویه یک ماهه"
        )
        val inv2Items = listOf(
            InvoiceItem(productName = "پرینتر لیزری چندکاره اچ‌پی M135w", quantity = 1.0, unit = "دستگاه", unitPrice = 19400000L, discountPercent = 0.0, totalRowPrice = 19400000L),
            InvoiceItem(productName = "کابل شبکه نگزنس Cat6 تمام مس", quantity = 2.0, unit = "حلقه ۳۰۵متر", unitPrice = 4200000L, discountPercent = 0.0, totalRowPrice = 8400000L)
        )
        database.invoiceDao().saveInvoiceWithItems(inv2, inv2Items)

        // Invoice 3: Check (چک صیادی)
        val inv3 = Invoice(
            invoiceNumber = "INV-1403-103",
            customerId = c3Id,
            customerName = "فروشگاه نگین کویر یزد",
            createdAt = "1403/08/10",
            dueDate = "1403/09/25",
            paymentType = "check",
            status = "paid",
            subtotal = 11650000L,
            discountPercent = 0.0,
            discountAmount = 0L,
            taxPercent = 10.0,
            taxAmount = 1165000L,
            totalAmount = 12815000L,
            paidAmount = 12815000L,
            remainingAmount = 0L,
            bankAccountId = b3,
            bankAccountName = "بانک ملی ایران",
            description = "دریافت چک صیادی بنفش به مبلغ کامل فاکتور",
            checkBankName = "بانک صادرات ایران",
            checkNumber = "۸۸۷۷۶۶۵۵",
            checkSayadiId = "۱۹۰۲۳۴۵۶۷۸۹۰۱۲۳۴",
            checkDueDate = "1403/09/25",
            checkIssuer = "اصغر نقی‌پور یزدی",
            checkAmount = 12815000L
        )
        val inv3Items = listOf(
            InvoiceItem(productName = "کیبورد و ماوس بی‌سیم لاجیتک MK270", quantity = 3.0, unit = "بسته", unitPrice = 1650000L, discountPercent = 0.0, totalRowPrice = 4950000L),
            InvoiceItem(productName = "کاغذ A4 کپی مکس ۸۰ گرمی", quantity = 5.0, unit = "کارتن ۵تایی", unitPrice = 1350000L, discountPercent = 0.0, totalRowPrice = 6750000L)
        )
        database.invoiceDao().saveInvoiceWithItems(inv3, inv3Items)

        // Invoice 4: Recent invoice (امروز)
        val todayStr = JalaliCalendar.getTodayString()
        val inv4 = Invoice(
            invoiceNumber = "INV-1403-104",
            customerId = c4Id,
            customerName = "خانم سارا کریمی (فریلنسر)",
            createdAt = todayStr,
            dueDate = todayStr,
            paymentType = "cash",
            cashPaymentMethod = "card_to_card",
            status = "paid",
            subtotal = 13450000L,
            discountPercent = 0.0,
            discountAmount = 0L,
            taxPercent = 10.0,
            taxAmount = 1345000L,
            totalAmount = 14795000L,
            paidAmount = 14795000L,
            remainingAmount = 0L,
            bankAccountId = b2,
            bankAccountName = "بانک سامان (بلوبانک)",
            description = "واریز کارت به کارت شتابی"
        )
        val inv4Items = listOf(
            InvoiceItem(productName = "مانیتور ۲۷ اینچ گیمینگ سامسونگ", quantity = 1.0, unit = "دستگاه", unitPrice = 11800000L, discountPercent = 0.0, totalRowPrice = 11800000L),
            InvoiceItem(productName = "کیبورد و ماوس بی‌سیم لاجیتک MK270", quantity = 1.0, unit = "بسته", unitPrice = 1650000L, discountPercent = 0.0, totalRowPrice = 1650000L)
        )
        database.invoiceDao().saveInvoiceWithItems(inv4, inv4Items)
    }
}
