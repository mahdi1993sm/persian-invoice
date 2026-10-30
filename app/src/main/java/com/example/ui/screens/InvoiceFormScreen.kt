package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BankAccount
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.Product
import com.example.ui.InvoiceViewModel
import com.example.ui.Screen
import com.example.ui.components.PersianDatePickerDialog
import com.example.ui.components.SearchInputField
import com.example.utils.JalaliCalendar
import com.example.utils.PersianNumberFormatter
import com.example.utils.ValidationUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceFormScreen(
    invoiceId: Long? = null,
    viewModel: InvoiceViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val defaultTax by viewModel.defaultTaxPercent.collectAsState()
    val invoicePrefix by viewModel.invoicePrefix.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    // Form States
    var invoiceNumber by remember { mutableStateOf("$invoicePrefix${System.currentTimeMillis() % 100000}") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var createdAt by remember { mutableStateOf(JalaliCalendar.getTodayString()) }
    var dueDate by remember { mutableStateOf(JalaliCalendar.getTodayString()) }
    var paymentType by remember { mutableStateOf("cash") } // cash, credit, check
    var cashMethod by remember { mutableStateOf("pos") } // pos, card_to_card, paya, satna, cash
    var selectedBankAccount by remember { mutableStateOf<BankAccount?>(null) }
    var description by remember { mutableStateOf("") }

    // Check specific fields
    var checkBankName by remember { mutableStateOf("") }
    var checkNumber by remember { mutableStateOf("") }
    var checkSayadiId by remember { mutableStateOf("") }
    var checkDueDate by remember { mutableStateOf(JalaliCalendar.getTodayString()) }
    var checkIssuer by remember { mutableStateOf("") }

    // Financial conditions state
    var paymentDate by remember { mutableStateOf(JalaliCalendar.getTodayString()) }
    var paymentMethod by remember { mutableStateOf("کارتخوان (پوز)") }
    var paymentAccountInfo by remember { mutableStateOf("") }
    var paymentTrackingCode by remember { mutableStateOf("") }

    // Items list
    val items = remember { mutableStateListOf<InvoiceItem>() }

    // Discount & Tax
    var discountPercentInput by remember { mutableStateOf("0") }
    var taxPercentInput by remember { mutableStateOf(defaultTax.toInt().toString()) }
    var paidAmountInput by remember { mutableStateOf("") }

    // Dialogs
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showDatePickerFor by remember { mutableStateOf<String?>(null) } // "created", "due", "checkDue", "paymentDate"
    var showNewCustomerDialog by remember { mutableStateOf(false) }

    // Validation error states
    var validationError by remember { mutableStateOf<String?>(null) }
    var customerError by remember { mutableStateOf(false) }
    var itemsError by remember { mutableStateOf(false) }
    var checkSayadiError by remember { mutableStateOf(false) }
    var checkBankError by remember { mutableStateOf(false) }
    var checkNumberError by remember { mutableStateOf(false) }

    // Load existing invoice if editing
    LaunchedEffect(invoiceId) {
        if (invoiceId != null && invoiceId > 0) {
            val existing = viewModel.getInvoiceById(invoiceId)
            if (existing != null) {
                invoiceNumber = existing.invoice.invoiceNumber
                selectedCustomer = customers.find { it.id == existing.invoice.customerId }
                    ?: Customer(id = existing.invoice.customerId, name = existing.invoice.customerName)
                createdAt = existing.invoice.createdAt
                dueDate = existing.invoice.dueDate
                paymentType = existing.invoice.paymentType
                cashMethod = existing.invoice.cashPaymentMethod
                selectedBankAccount = bankAccounts.find { it.id == existing.invoice.bankAccountId }
                description = existing.invoice.description
                checkBankName = existing.invoice.checkBankName
                checkNumber = existing.invoice.checkNumber
                checkSayadiId = existing.invoice.checkSayadiId
                checkDueDate = existing.invoice.checkDueDate
                checkIssuer = existing.invoice.checkIssuer
                paymentDate = existing.invoice.paymentDate.ifBlank { existing.invoice.createdAt }
                paymentMethod = existing.invoice.paymentMethod.ifBlank {
                    when (existing.invoice.paymentType) {
                        "cash" -> when (existing.invoice.cashPaymentMethod) {
                            "pos" -> "کارتخوان (پوز)"
                            "card_to_card" -> "کارت به کارت"
                            "paya" -> "پایا / ساتنا"
                            else -> "نقدی (اسکناس)"
                        }
                        "credit" -> "نسیه"
                        "check" -> "چک صیادی"
                        else -> "کارتخوان (پوز)"
                    }
                }
                paymentAccountInfo = existing.invoice.paymentAccountInfo.ifBlank { existing.invoice.bankAccountName }
                paymentTrackingCode = existing.invoice.paymentTrackingCode
                discountPercentInput = existing.invoice.discountPercent.toInt().toString()
                taxPercentInput = existing.invoice.taxPercent.toInt().toString()
                paidAmountInput = if (existing.invoice.paidAmount > 0) existing.invoice.paidAmount.toString() else ""

                items.clear()
                items.addAll(existing.items)
            }
        }
    }

    // Mathematical calculations
    val subtotal = remember(items.toList()) {
        items.sumOf { it.totalRowPrice }
    }

    val discountPercent = discountPercentInput.toDoubleOrNull() ?: 0.0
    val discountAmount = (subtotal * (discountPercent / 100.0)).toLong()

    val subtotalAfterDiscount = (subtotal - discountAmount).coerceAtLeast(0L)
    val taxPercent = taxPercentInput.toDoubleOrNull() ?: 0.0
    val taxAmount = (subtotalAfterDiscount * (taxPercent / 100.0)).toLong()

    val totalAmount = subtotalAfterDiscount + taxAmount

    val enteredPaidAmount = paidAmountInput.toLongOrNull()
    val effectivePaidAmount = when {
        paymentType == "credit" -> enteredPaidAmount ?: 0L
        enteredPaidAmount != null -> enteredPaidAmount
        paymentType == "cash" || paymentType == "check" -> totalAmount
        else -> 0L
    }

    val remainingAmount = (totalAmount - effectivePaidAmount).coerceAtLeast(0L)
    val calculatedStatus = when {
        remainingAmount == 0L -> "paid"
        effectivePaidAmount == 0L -> "credit"
        else -> "partial"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (invoiceId == null) "صدور فاکتور فروش" else "ویرایش فاکتور") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("invoice_form_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // Validation checks
                            if (selectedCustomer == null) {
                                customerError = true
                                validationError = "لطفاً خریدار (مشتری) را انتخاب کنید."
                                coroutineScope.launch {
                                    listState.animateScrollToItem(1)
                                }
                                return@Button
                            }
                            if (items.isEmpty()) {
                                itemsError = true
                                validationError = "حداقل باید یک قلم کالا یا خدمات در فاکتور ثبت شود."
                                coroutineScope.launch {
                                    listState.animateScrollToItem(2)
                                }
                                return@Button
                            }
                            if (paymentType == "check" || paymentMethod == "چک صیادی") {
                                var hasCheckError = false
                                if (checkSayadiId.length != 16) {
                                    checkSayadiError = true
                                    hasCheckError = true
                                }
                                if (checkBankName.isBlank()) {
                                    checkBankError = true
                                    hasCheckError = true
                                }
                                if (checkNumber.isBlank()) {
                                    checkNumberError = true
                                    hasCheckError = true
                                }
                                if (hasCheckError) {
                                    validationError = "لطفاً مشخصات الزامی چک صیادی را تکمیل نمایید."
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(4)
                                    }
                                    return@Button
                                }
                            }

                            validationError = null
                            customerError = false
                            itemsError = false
                            checkSayadiError = false
                            checkBankError = false
                            checkNumberError = false

                            val invoiceToSave = Invoice(
                                id = invoiceId ?: 0L,
                                invoiceNumber = invoiceNumber,
                                customerId = selectedCustomer!!.id,
                                customerName = selectedCustomer!!.name,
                                createdAt = createdAt,
                                dueDate = dueDate,
                                paymentType = paymentType,
                                cashPaymentMethod = cashMethod,
                                status = calculatedStatus,
                                subtotal = subtotal,
                                discountPercent = discountPercent,
                                discountAmount = discountAmount,
                                taxPercent = taxPercent,
                                taxAmount = taxAmount,
                                totalAmount = totalAmount,
                                paidAmount = effectivePaidAmount,
                                remainingAmount = remainingAmount,
                                bankAccountId = selectedBankAccount?.id,
                                bankAccountName = selectedBankAccount?.bankName ?: "",
                                description = description,
                                paymentDate = paymentDate,
                                paymentMethod = paymentMethod,
                                paymentAccountInfo = paymentAccountInfo.ifBlank { selectedBankAccount?.bankName ?: "" },
                                paymentTrackingCode = paymentTrackingCode,
                                checkBankName = checkBankName,
                                checkNumber = checkNumber,
                                checkSayadiId = checkSayadiId,
                                checkDueDate = checkDueDate,
                                checkIssuer = checkIssuer,
                                checkAmount = if (paymentType == "check") totalAmount else 0L
                            )

                            viewModel.saveInvoice(invoiceToSave, items.toList()) { savedId ->
                                viewModel.navigateTo(Screen.InvoiceView(savedId))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_invoice_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ثبت فاکتور")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .testTag("invoice_form_scrollable"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Validation Error Alert
            if (validationError != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626))
                            Text(
                                text = validationError!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Section 1: Invoice Header Info
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "اطلاعات فاکتور و تاریخ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = { Text("شماره فاکتور") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_invoice_number")
                            )

                            OutlinedCard(
                                onClick = { showDatePickerFor = "created" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("picker_created_date")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("تاریخ صدور", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(createdAt, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp), tint = themeColor)
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Customer Selection
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (customerError) Color(0xFFFFF1F2)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (customerError) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_selection_section")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مشخصات خریدار (مشتری)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (customerError) MaterialTheme.colorScheme.error else themeColor
                            )
                            TextButton(
                                onClick = { showCustomerDialog = true },
                                modifier = Modifier.testTag("select_customer_button")
                            ) {
                                Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedCustomer == null) "انتخاب مشتری" else "تغییر مشتری")
                            }
                        }

                        if (selectedCustomer != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedCustomer!!.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (selectedCustomer!!.mobile.isNotBlank() || selectedCustomer!!.phone.isNotBlank()) {
                                            Text(
                                                text = "تماس: ${selectedCustomer!!.mobile.ifBlank { selectedCustomer!!.phone }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (selectedCustomer!!.address.isNotBlank()) {
                                            Text(
                                                text = "آدرس: ${selectedCustomer!!.address}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = { selectedCustomer = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "حذف انتخاب", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showCustomerDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                border = if (customerError) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = if (customerError) MaterialTheme.colorScheme.error else LocalContentColor.current
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "برای صدور فاکتور، ابتدا مشتری را انتخاب کنید",
                                    color = if (customerError) MaterialTheme.colorScheme.error else Color.Unspecified
                                )
                            }
                            if (customerError) {
                                Text(
                                    text = "این فیلد اجباری است — لطفاً خریدار فاکتور را مشخص نمایید.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Invoice Items Table
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (itemsError) Color(0xFFFFF1F2)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (itemsError) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اقلام فاکتور (${items.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (itemsError) MaterialTheme.colorScheme.error else themeColor
                            )
                            Button(
                                onClick = { showAddItemDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                modifier = Modifier.testTag("add_item_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("افزودن کالا")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (items.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "هنوز کالایی به فاکتور اضافه نشده است.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (itemsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (itemsError) {
                                        Text(
                                            text = "این بخش اجباری است — حداقل باید یک ردیف کالا یا خدمات ثبت شود.",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items.forEachIndexed { index, item ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(themeColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColor
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = item.productName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${item.quantity} ${item.unit} × ${PersianNumberFormatter.formatPrice(item.unitPrice, currency)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = PersianNumberFormatter.formatPrice(item.totalRowPrice, currency),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeColor
                                                )
                                                IconButton(
                                                    onClick = { items.removeAt(index) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Discount, Tax & Totals Summary
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "محاسبات مالی",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = discountPercentInput,
                                onValueChange = { discountPercentInput = ValidationUtil.cleanDecimal(it) },
                                label = { Text("درصد تخفیف کلی") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_discount_percent")
                            )

                            OutlinedTextField(
                                value = taxPercentInput,
                                onValueChange = { taxPercentInput = ValidationUtil.cleanDecimal(it) },
                                label = { Text("درصد مالیات بر ارزش افزوده") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_tax_percent")
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        // Summary Rows
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("جمع اقلام:", style = MaterialTheme.typography.bodyMedium)
                            Text(PersianNumberFormatter.formatPrice(subtotal, currency), style = MaterialTheme.typography.bodyMedium)
                        }

                        if (discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("مبلغ تخفیف ($discountPercent%):", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF10B981))
                                Text("- " + PersianNumberFormatter.formatPrice(discountAmount, currency), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF10B981))
                            }
                        }

                        if (taxAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("مالیات ($taxPercent%):", style = MaterialTheme.typography.bodyMedium)
                                Text("+ " + PersianNumberFormatter.formatPrice(taxAmount, currency), style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مبلغ کل قابل پرداخت:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = PersianNumberFormatter.formatPrice(totalAmount, currency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }
                }
            }

            // Section 5: Payment & Settlement
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "شرایط مالی و نحوه تسویه فاکتور",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )

                        // 1. Settlement Type Selector (Cash / Credit / Check)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("cash" to "نقدی (تسویه)", "credit" to "نسیه / اقساط", "check" to "چک صیادی").forEach { (type, label) ->
                                val isSelected = paymentType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        paymentType = type
                                        if (type == "check") {
                                            paymentMethod = "چک صیادی"
                                        } else if (type == "credit" && paymentMethod == "چک صیادی") {
                                            paymentMethod = "کارت به کارت"
                                        }
                                    },
                                    label = { Text(label) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                        selectedLabelColor = themeColor
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 2. Financial Amount & Payment Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = paidAmountInput,
                                onValueChange = { paidAmountInput = ValidationUtil.cleanDigits(it) },
                                label = { Text("مبلغ پرداختی") },
                                placeholder = { Text(totalAmount.toString()) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("input_paid_amount")
                            )

                            OutlinedCard(
                                onClick = { showDatePickerFor = "paymentDate" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("تاریخ واریز", style = MaterialTheme.typography.labelSmall)
                                        Text(paymentDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp), tint = themeColor)
                                }
                            }
                        }

                        // Quick full payment button if amount isn't full
                        if (effectivePaidAmount < totalAmount) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "مانده قابل تسویه: ${PersianNumberFormatter.formatPrice(remainingAmount, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFEF4444)
                                )
                                TextButton(
                                    onClick = { paidAmountInput = totalAmount.toString() }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تسویه کل مبلغ فاکتور", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // 3. Payment Method Selection Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("روش پرداخت و واریز:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val methods = listOf(
                                    "کارتخوان (پوز)" to "pos",
                                    "کارت به کارت" to "card_to_card",
                                    "واریز به حساب" to "paya",
                                    "نقدی (اسکناس)" to "cash",
                                    "چک صیادی" to "check"
                                )
                                methods.forEach { (name, methodKey) ->
                                    val isSelected = paymentMethod == name || (paymentType == "check" && name == "چک صیادی")
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            paymentMethod = name
                                            cashMethod = methodKey
                                            if (name == "چک صیادی") paymentType = "check"
                                        },
                                        label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        // 4. Target Bank Account (Which account received the payment)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("حساب مقصد واریزی فروشگاه:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            if (bankAccounts.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    bankAccounts.forEach { acc ->
                                        val isSelected = selectedBankAccount?.id == acc.id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedBankAccount = acc
                                                paymentAccountInfo = "${acc.bankName} (${acc.ownerName.ifBlank { "فروشگاه" }}) - ${acc.cardNumber.ifBlank { acc.accountNumber }}"
                                            },
                                            label = { Text(acc.bankName, style = MaterialTheme.typography.labelSmall) },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = paymentAccountInfo,
                                onValueChange = { paymentAccountInfo = ValidationUtil.cleanSingleLine(it) },
                                label = { Text("مشخصات حساب / کارت مقصد واریزی") },
                                placeholder = { Text("مثلاً بانک ملت - شماره کارت ۶۰۳۷... بنام سپهر حسینی") },
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_payment_account_info")
                            )
                        }

                        // 5. Transaction / Tracking Reference Code
                        OutlinedTextField(
                            value = paymentTrackingCode,
                            onValueChange = { paymentTrackingCode = ValidationUtil.cleanDigits(it) },
                            label = { Text("شماره پیگیری و کد رهگیری تراکنش") },
                            placeholder = { Text("مثلاً شماره ارجاع رسید کارتخوان یا انتقال کارت به کارت") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_payment_tracking_code")
                        )

                        // 6. Credit Due Date (if credit or remaining balance)
                        if (paymentType == "credit" || remainingAmount > 0) {
                            OutlinedCard(
                                onClick = { showDatePickerFor = "due" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("تاریخ سررسید نسیه / مانده بدهی", style = MaterialTheme.typography.labelSmall)
                                        Text(dueDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = themeColor)
                                }
                            }
                        }

                        // 7. Check Details (if paymentType == "check" or paymentMethod == "چک صیادی")
                        if (paymentType == "check" || paymentMethod == "چک صیادی") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = checkSayadiId,
                                    onValueChange = {
                                        checkSayadiId = ValidationUtil.cleanDigits(it).take(16)
                                        checkSayadiError = false
                                        validationError = null
                                    },
                                    label = { Text("شناسه ۱۶ رقمی صیادی") },
                                    placeholder = { Text("شناسه چک صیاد بنفش") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    maxLines = 1,
                                    isError = checkSayadiError,
                                    supportingText = {
                                        if (checkSayadiError) {
                                            Text(
                                                text = if (checkSayadiId.isBlank()) "این فیلد اجباری است — لطفاً شناسه ۱۶ رقمی صیادی را وارد کنید"
                                                else "شناسه صیادی باید دقیقاً ۱۶ رقم باشد (${checkSayadiId.length}/۱۶)",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = checkBankName,
                                        onValueChange = {
                                            checkBankName = ValidationUtil.cleanSingleLine(it)
                                            checkBankError = false
                                            validationError = null
                                        },
                                        label = { Text("بانک صادرکننده") },
                                        singleLine = true,
                                        maxLines = 1,
                                        isError = checkBankError,
                                        supportingText = {
                                            if (checkBankError) {
                                                Text("نام بانک الزامی است", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = checkNumber,
                                        onValueChange = {
                                            checkNumber = ValidationUtil.cleanDigits(it).take(20)
                                            checkNumberError = false
                                            validationError = null
                                        },
                                        label = { Text("شماره چک") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        maxLines = 1,
                                        isError = checkNumberError,
                                        supportingText = {
                                            if (checkNumberError) {
                                                Text("شماره چک الزامی است", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = checkIssuer,
                                        onValueChange = { checkIssuer = ValidationUtil.cleanSingleLine(it) },
                                        label = { Text("نام صاحب چک") },
                                        singleLine = true,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedCard(
                                        onClick = { showDatePickerFor = "checkDue" },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("سررسید چک", style = MaterialTheme.typography.labelSmall)
                                                Text(checkDueDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            }
                                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp), tint = themeColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 6: Notes & Conditions
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "توضیحات و شرایط فروش",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("توضیحات اضافی، شرایط گارانتی یا تحویل کالا...") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_invoice_description")
                        )
                    }
                }
            }
        }
    }

    // Customer Selection Dialog
    if (showCustomerDialog) {
        var customerSearch by remember { mutableStateOf("") }
        val filteredCustomers = remember(customers, customerSearch) {
            customers.filter {
                customerSearch.isBlank() || it.name.contains(customerSearch, ignoreCase = true) || it.mobile.contains(customerSearch)
            }
        }

        Dialog(onDismissRequest = { showCustomerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("انتخاب خریدار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showCustomerDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "بستن")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SearchInputField(
                        query = customerSearch,
                        onQueryChange = { customerSearch = it },
                        placeholder = "جستجوی نام یا موبایل مشتری..."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showCustomerDialog = false
                            showNewCustomerDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت مشتری جدید")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCustomers, key = { it.id }) { cust ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCustomer = cust
                                        showCustomerDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(cust.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        if (cust.mobile.isNotBlank()) {
                                            Text(cust.mobile, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = themeColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Item Dialog (Pick from inventory OR custom item)
    if (showAddItemDialog) {
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var itemName by remember { mutableStateOf("") }
        var itemQuantity by remember { mutableStateOf("1") }
        var itemUnit by remember { mutableStateOf("عدد") }
        var itemPrice by remember { mutableStateOf("") }
        var itemDiscount by remember { mutableStateOf("0") }
        var productSearch by remember { mutableStateOf("") }
        var itemNameError by remember { mutableStateOf(false) }
        var itemPriceError by remember { mutableStateOf(false) }
        val addItemScrollState = rememberScrollState()

        val filteredCatalog = remember(products, productSearch) {
            products.filter {
                productSearch.isBlank() || it.name.contains(productSearch, ignoreCase = true) || it.code.contains(productSearch, ignoreCase = true)
            }
        }

        Dialog(onDismissRequest = { showAddItemDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 580.dp)
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // 1. Pinned Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("افزودن کالا به فاکتور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { showAddItemDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "بستن")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Scrollable Body
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(addItemScrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Select from catalog or enter name
                        if (products.isNotEmpty()) {
                            Text("انتخاب از انبار کالای ثبت‌شده:", style = MaterialTheme.typography.labelSmall)
                            SearchInputField(
                                query = productSearch,
                                onQueryChange = { productSearch = it },
                                placeholder = "جستجوی کالا در انبار..."
                            )

                            LazyColumn(
                                modifier = Modifier.height(85.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredCatalog.take(5), key = { it.id }) { prod ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selectedProduct?.id == prod.id) themeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedProduct = prod
                                                itemName = prod.name
                                                itemPrice = prod.price.toString()
                                                itemUnit = prod.unit
                                                itemNameError = false
                                                itemPriceError = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(prod.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            Text(PersianNumberFormatter.formatPrice(prod.price, currency), style = MaterialTheme.typography.bodySmall, color = themeColor)
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = itemName,
                            onValueChange = {
                                itemName = ValidationUtil.cleanSingleLine(it)
                                itemNameError = false
                            },
                            label = { Text("نام کالا یا خدمات") },
                            singleLine = true,
                            maxLines = 1,
                            isError = itemNameError,
                            supportingText = {
                                if (itemNameError) {
                                    Text("این فیلد اجباری است — لطفاً نام کالا یا خدمات را وارد کنید", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = itemQuantity,
                                onValueChange = { itemQuantity = ValidationUtil.cleanDecimal(it) },
                                label = { Text("تعداد / مقدار") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = itemUnit,
                                onValueChange = { itemUnit = ValidationUtil.cleanSingleLine(it) },
                                label = { Text("واحد") },
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = itemPrice,
                            onValueChange = {
                                itemPrice = ValidationUtil.cleanDigits(it)
                                itemPriceError = false
                            },
                            label = { Text("قیمت واحد") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            maxLines = 1,
                            isError = itemPriceError,
                            supportingText = {
                                if (itemPriceError) {
                                    Text("این فیلد اجباری است — قیمت واحد باید بزرگتر از صفر باشد", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Pinned Bottom Action Button
                    Button(
                        onClick = {
                            val qty = itemQuantity.toDoubleOrNull() ?: 1.0
                            val prc = itemPrice.toLongOrNull() ?: 0L
                            val disc = itemDiscount.toDoubleOrNull() ?: 0.0
                            val rowTotal = (qty * prc * (1 - disc / 100.0)).toLong()

                            var hasError = false
                            if (itemName.isBlank()) {
                                itemNameError = true
                                hasError = true
                            }
                            if (prc <= 0) {
                                itemPriceError = true
                                hasError = true
                            }

                            if (!hasError) {
                                items.add(
                                    InvoiceItem(
                                        productId = selectedProduct?.id,
                                        productName = itemName,
                                        quantity = qty,
                                        unit = itemUnit,
                                        unitPrice = prc,
                                        discountPercent = disc,
                                        totalRowPrice = rowTotal
                                    )
                                )
                                itemsError = false
                                validationError = null
                                showAddItemDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("ثبت و افزودن به جدول", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Quick New Customer Dialog
    if (showNewCustomerDialog) {
        var newName by remember { mutableStateOf("") }
        var newMobile by remember { mutableStateOf("") }
        var newAddress by remember { mutableStateOf("") }
        var newNameError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showNewCustomerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("ثبت سریع مشتری جدید", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = ValidationUtil.cleanSingleLine(it)
                            newNameError = false
                        },
                        label = { Text("نام شخص یا شرکت") },
                        singleLine = true,
                        maxLines = 1,
                        isError = newNameError,
                        supportingText = {
                            if (newNameError) {
                                Text("این فیلد اجباری است — لطفاً نام شخص یا شرکت را وارد کنید", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val isNewMobileValid = newMobile.isBlank() || ValidationUtil.isValidMobile(newMobile)

                    OutlinedTextField(
                        value = newMobile,
                        onValueChange = { newMobile = ValidationUtil.cleanMobile(it) },
                        label = { Text("شماره همراه") },
                        placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        maxLines = 1,
                        isError = !isNewMobileValid,
                        supportingText = {
                            if (!isNewMobileValid) {
                                Text("باید ۱۱ رقم و با ۰۹ شروع شود", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("آدرس") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showNewCustomerDialog = false }) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                if (newName.isBlank()) {
                                    newNameError = true
                                    return@Button
                                }
                                if (newMobile.isNotBlank() && !ValidationUtil.isValidMobile(newMobile)) {
                                    return@Button
                                }
                                val newCustomer = Customer(
                                    name = newName.trim(),
                                    mobile = newMobile.trim(),
                                    address = newAddress.trim(),
                                    createdAt = JalaliCalendar.getTodayString()
                                )
                                viewModel.saveCustomer(newCustomer) {
                                    selectedCustomer = newCustomer
                                    customerError = false
                                    validationError = null
                                    showNewCustomerDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("ذخیره و انتخاب")
                        }
                    }
                }
            }
        }
    }

    // Persian Date Picker Dialog
    if (showDatePickerFor != null) {
        val initial = when (showDatePickerFor) {
            "created" -> createdAt
            "due" -> dueDate
            "checkDue" -> checkDueDate
            "paymentDate" -> paymentDate
            else -> JalaliCalendar.getTodayString()
        }
        PersianDatePickerDialog(
            initialDate = initial,
            onDateSelected = { picked ->
                when (showDatePickerFor) {
                    "created" -> createdAt = picked
                    "due" -> dueDate = picked
                    "checkDue" -> checkDueDate = picked
                    "paymentDate" -> paymentDate = picked
                }
            },
            onDismissRequest = { showDatePickerFor = null }
        )
    }
}
