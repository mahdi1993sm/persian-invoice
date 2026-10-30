package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.model.BankAccount
import com.example.ui.InvoiceViewModel
import com.example.ui.components.EmptyStateView
import com.example.utils.ValidationUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(viewModel: InvoiceViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var showAddDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<BankAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<BankAccount?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "حساب‌های بانکی فروشگاه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("bank_accounts_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.AddCard, contentDescription = null) },
                text = { Text("حساب بانکی جدید") },
                containerColor = themeColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_bank_account")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .testTag("bank_accounts_screen")
        ) {
            Text(
                text = "برای کپی سریع شماره کارت یا شماره شبا، روی آن ضربه بزنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (bankAccounts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CreditCard,
                    title = "حساب بانکی ثبت نشده است",
                    description = "شماره حساب و کارت‌های بانکی فروشگاه خود را جهت درج در فاکتورها ثبت کنید.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(bankAccounts, key = { it.id }) { acc ->
                        val cardGradients = listOf(
                            listOf(Color(0xFFDC2626), Color(0xFF991B1B)), // Mellat Red
                            listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)), // Blu / Saman Blue
                            listOf(Color(0xFF0D9488), Color(0xFF115E59)), // Melli Teal
                            listOf(Color(0xFF7C3AED), Color(0xFF5B21B6)), // Pasargad Purple
                            listOf(Color(0xFFEA580C), Color(0xFFC2410C))  // Sepah Orange
                        )
                        val gradientColors = cardGradients[acc.colorIndex % cardGradients.size]

                        // Debit Card Layout
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .testTag("bank_card_${acc.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(gradientColors))
                                    .padding(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Top Row: Bank Name + Action Icons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White)
                                            Text(
                                                text = acc.bankName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = { accountToEdit = acc },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { accountToDelete = acc },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    // Middle Row: 16-Digit Card Number (Clickable to copy)
                                    if (acc.cardNumber.isNotBlank()) {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    copyToClipboard("شماره کارت", acc.cardNumber)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = acc.cardNumber,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 2.sp,
                                                    color = Color.White
                                                )
                                                Icon(Icons.Default.ContentCopy, contentDescription = "کپی", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    // Bottom Row: Owner name & Sheba number
                                    Column {
                                        if (acc.shebaNumber.isNotBlank()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        copyToClipboard("شماره شبا", acc.shebaNumber)
                                                    },
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "شبا: ${acc.shebaNumber}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                                Text("کپی شبا", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = acc.ownerName.ifBlank { "صاحب حساب" },
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            if (acc.accountNumber.isNotBlank()) {
                                                Text(
                                                    text = "حساب: ${acc.accountNumber}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f)
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
    }

    // Add / Edit Account Dialog
    if (showAddDialog || accountToEdit != null) {
        val editing = accountToEdit
        var bankName by remember { mutableStateOf(editing?.bankName ?: "بانک ملت") }
        var ownerName by remember { mutableStateOf(editing?.ownerName ?: "") }
        var cardNumber by remember { mutableStateOf(editing?.cardNumber ?: "") }
        var shebaNumber by remember { mutableStateOf(editing?.shebaNumber ?: "IR") }
        var accountNumber by remember { mutableStateOf(editing?.accountNumber ?: "") }
        var colorIndex by remember { mutableIntStateOf(editing?.colorIndex ?: 0) }
        var error by remember { mutableStateOf<String?>(null) }
        var bankNameError by remember { mutableStateOf(false) }

        val dialogScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        val commonBanks = listOf("بانک ملت", "بانک ملی", "بانک سامان (بلوبانک)", "بانک صادرات", "بانک تجارت", "بانک پاسارگاد", "بانک سپه", "بانک رسالت")

        Dialog(
            onDismissRequest = {
                showAddDialog = false
                accountToEdit = null
            }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(dialogScrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (editing == null) "ثبت حساب بانکی جدید" else "ویرایش حساب بانکی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )

                    if (error != null) {
                        Text(error!!, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = {
                            bankName = ValidationUtil.cleanSingleLine(it)
                            bankNameError = false
                            error = null
                        },
                        label = { Text("نام بانک (الزامی)") },
                        singleLine = true,
                        maxLines = 1,
                        isError = bankNameError,
                        supportingText = {
                            if (bankNameError) {
                                Text("این فیلد اجباری است — لطفاً نام بانک را وارد کنید", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = ValidationUtil.cleanSingleLine(it) },
                        label = { Text("نام صاحب حساب") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = ValidationUtil.cleanDigits(it).take(16) },
                        label = { Text("شماره کارت ۱۶ رقمی") },
                        placeholder = { Text("۶۰۳۷-xxxx-xxxx-xxxx") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = shebaNumber,
                        onValueChange = { shebaNumber = ValidationUtil.cleanSingleLine(it).take(26) },
                        label = { Text("شماره شبا (۲۴ رقم)") },
                        placeholder = { Text("IR...") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = ValidationUtil.cleanDigits(it).take(20) },
                        label = { Text("شماره حساب") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

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
                                accountToEdit = null
                            }
                        ) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (bankName.isBlank()) {
                                    bankNameError = true
                                    error = "نام بانک نمی‌تواند خالی باشد."
                                    coroutineScope.launch { dialogScrollState.animateScrollTo(0) }
                                    return@Button
                                }
                                val toSave = BankAccount(
                                    id = editing?.id ?: 0L,
                                    bankName = bankName,
                                    ownerName = ownerName,
                                    cardNumber = cardNumber,
                                    shebaNumber = shebaNumber,
                                    accountNumber = accountNumber,
                                    colorIndex = (0..4).random()
                                )
                                viewModel.saveBankAccount(toSave) {
                                    showAddDialog = false
                                    accountToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("ذخیره حساب")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (accountToDelete != null) {
        val target = accountToDelete!!
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("حذف حساب بانکی") },
            text = { Text("آیا از حذف حساب «${target.bankName}» اطمینان دارید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBankAccount(target)
                        accountToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { accountToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}
