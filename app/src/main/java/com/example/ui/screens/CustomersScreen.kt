package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.data.model.Customer
import com.example.ui.InvoiceViewModel
import com.example.ui.Screen
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchInputField
import com.example.utils.JalaliCalendar
import com.example.utils.PersianNumberFormatter
import com.example.utils.ValidationUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(viewModel: InvoiceViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val customers by viewModel.customers.collectAsState()
    val invoices by viewModel.invoicesWithItems.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var searchQuery by remember { mutableStateOf("") }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.mobile.contains(searchQuery) ||
                    it.phone.contains(searchQuery) ||
                    it.city.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("مشتری جدید") },
                containerColor = themeColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_customer")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .testTag("customers_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            SearchInputField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "جستجوی نام، تلفن یا شهر مشتری..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${filteredCustomers.size} مشتری ثبت‌شده",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredCustomers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PeopleOutline,
                    title = "مشتری یافت نشد",
                    description = if (searchQuery.isNotEmpty()) "نتیجه‌ای برای جستجوی شما پیدا نشد."
                    else "هنوز مشتری ثبت نکرده‌اید. با زدن دکمه «مشتری جدید» اضافه کنید.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { cust ->
                        // Calculate customer spending and debt dynamically from all recorded invoices
                        val customerInvoices = invoices.filter {
                            it.invoice.customerId == cust.id ||
                            (it.invoice.customerName.isNotBlank() && it.invoice.customerName.trim().equals(cust.name.trim(), ignoreCase = true))
                        }
                        val totalPurchases = customerInvoices.sumOf { it.invoice.totalAmount }
                        val totalDebt = customerInvoices.sumOf { it.invoice.remainingAmount }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_card_${cust.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(themeColor.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = cust.name.firstOrNull()?.toString() ?: "م",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColor
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = cust.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (cust.city.isNotBlank()) {
                                                Text(
                                                    text = "شهر: ${cust.city}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { customerToEdit = cust },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { customerToDelete = cust },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                if (cust.mobile.isNotBlank() || cust.phone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = cust.mobile.ifBlank { cust.phone },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (cust.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cust.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                // Purchase stats for this customer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${customerInvoices.size} فاکتور ثبت‌شده", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "خرید: " + PersianNumberFormatter.formatPrice(totalPurchases, currency),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColor
                                        )
                                    }

                                    if (customerInvoices.isEmpty()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "بدون گردش حساب",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else if (totalDebt > 0) {
                                        Surface(
                                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("بدهکار", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = PersianNumberFormatter.formatPrice(totalDebt, currency),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            color = Color(0xFF10B981).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "تسویه کامل",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
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

    // Add / Edit Customer Dialog
    if (showAddDialog || customerToEdit != null) {
        val editing = customerToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var mobile by remember { mutableStateOf(editing?.mobile ?: "") }
        var phone by remember { mutableStateOf(editing?.phone ?: "") }
        var province by remember { mutableStateOf(editing?.province ?: "") }
        var city by remember { mutableStateOf(editing?.city ?: "") }
        var address by remember { mutableStateOf(editing?.address ?: "") }
        var postalCode by remember { mutableStateOf(editing?.postalCode ?: "") }
        var nationalCode by remember { mutableStateOf(editing?.nationalCode ?: "") }
        var economicCode by remember { mutableStateOf(editing?.economicCode ?: "") }
        var error by remember { mutableStateOf<String?>(null) }
        var nameError by remember { mutableStateOf(false) }

        val dialogScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        Dialog(
            onDismissRequest = {
                showAddDialog = false
                customerToEdit = null
            }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(dialogScrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (editing == null) "ثبت مشتری جدید" else "ویرایش مشخصات مشتری",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )

                    if (error != null) {
                        Text(error!!, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                            error = null
                        },
                        label = { Text("نام مشتری یا شرکت") },
                        singleLine = true,
                        isError = nameError,
                        supportingText = {
                            if (nameError) {
                                Text("این فیلد اجباری است — لطفاً نام مشتری را وارد کنید", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val isMobileValid = mobile.isBlank() || ValidationUtil.isValidMobile(mobile)
                    val isNationalValid = nationalCode.isBlank() || ValidationUtil.isValidNationalCode(nationalCode)
                    val isPostalValid = postalCode.isBlank() || ValidationUtil.isValidPostalCode(postalCode)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = ValidationUtil.cleanMobile(it) },
                            label = { Text("تلفن همراه") },
                            placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            maxLines = 1,
                            isError = !isMobileValid,
                            supportingText = {
                                if (!isMobileValid) {
                                    Text("باید ۱۱ رقم و با ۰۹ شروع شود (${mobile.length}/۱۱)", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                                } else if (mobile.isNotBlank()) {
                                    Text("${mobile.length}/۱۱ رقم", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = ValidationUtil.cleanDigits(it).take(11) },
                            label = { Text("تلفن ثابت") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = province,
                            onValueChange = { province = ValidationUtil.cleanSingleLine(it) },
                            label = { Text("استان") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = ValidationUtil.cleanSingleLine(it) },
                            label = { Text("شهر") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس پستی") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nationalCode,
                            onValueChange = { nationalCode = ValidationUtil.cleanNationalCode(it) },
                            label = { Text("کد ملی") },
                            placeholder = { Text("۱۰ رقم عددی") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            maxLines = 1,
                            isError = !isNationalValid,
                            supportingText = {
                                if (!isNationalValid) {
                                    Text("کد ملی باید ۱۰ رقم باشد (${nationalCode.length}/۱۰)", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                                } else if (nationalCode.isNotBlank()) {
                                    Text("${nationalCode.length}/۱۰ رقم", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = postalCode,
                            onValueChange = { postalCode = ValidationUtil.cleanPostalCode(it) },
                            label = { Text("کد پستی") },
                            placeholder = { Text("۱۰ رقم عددی") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            maxLines = 1,
                            isError = !isPostalValid,
                            supportingText = {
                                if (!isPostalValid) {
                                    Text("کد پستی باید ۱۰ رقم باشد (${postalCode.length}/۱۰)", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                                } else if (postalCode.isNotBlank()) {
                                    Text("${postalCode.length}/۱۰ رقم", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showAddDialog = false
                                customerToEdit = null
                            }
                        ) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    nameError = true
                                    error = "نام مشتری نمی‌تواند خالی باشد."
                                    coroutineScope.launch { dialogScrollState.animateScrollTo(0) }
                                    return@Button
                                }
                                if (mobile.isNotBlank() && !ValidationUtil.isValidMobile(mobile)) {
                                    error = "شماره موبایل باید ۱۱ رقم بوده و با ۰۹ آغاز شود."
                                    coroutineScope.launch { dialogScrollState.animateScrollTo(0) }
                                    return@Button
                                }
                                if (nationalCode.isNotBlank() && !ValidationUtil.isValidNationalCode(nationalCode)) {
                                    error = "کد ملی باید دقیقاً ۱۰ رقم عددی باشد."
                                    coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.maxValue) }
                                    return@Button
                                }
                                if (postalCode.isNotBlank() && !ValidationUtil.isValidPostalCode(postalCode)) {
                                    error = "کد پستی باید دقیقاً ۱۰ رقم عددی باشد."
                                    coroutineScope.launch { dialogScrollState.animateScrollTo(dialogScrollState.maxValue) }
                                    return@Button
                                }
                                val toSave = Customer(
                                    id = editing?.id ?: 0L,
                                    name = name.trim(),
                                    mobile = mobile.trim(),
                                    phone = phone.trim(),
                                    province = province.trim(),
                                    city = city.trim(),
                                    address = address.trim(),
                                    postalCode = postalCode.trim(),
                                    nationalCode = nationalCode.trim(),
                                    economicCode = economicCode.trim(),
                                    createdAt = editing?.createdAt ?: JalaliCalendar.getTodayString()
                                )
                                viewModel.saveCustomer(toSave) {
                                    showAddDialog = false
                                    customerToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("ذخیره اطلاعات")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (customerToDelete != null) {
        val target = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("حذف مشتری") },
            text = { Text("آیا از حذف مشتری «${target.name}» مطمئن هستید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(target)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { customerToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}
