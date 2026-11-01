package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.InvoiceViewModel
import com.example.ui.Screen
import com.example.ui.theme.ThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: InvoiceViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val activeThemeHex by viewModel.themeColorHex.collectAsState()
    val isDarkMode by viewModel.darkMode.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val defaultTax by viewModel.defaultTaxPercent.collectAsState()
    val invoicePrefix by viewModel.invoicePrefix.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var taxInput by remember { mutableStateOf(defaultTax.toInt().toString()) }
    var prefixInput by remember { mutableStateOf(invoicePrefix) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات برنامه") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("settings_screen_scrollable"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Colors Section
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "رنگ قالب و پوسته برنامه (۸ تم رنگی)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Text(
                        text = "رنگ اصلی فاکتورها، دکمه‌ها و منوهای برنامه را سفارشی‌سازی کنید:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeColors.take(4).forEach { option ->
                            val isSelected = option.hex.equals(activeThemeHex, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.setThemeColor(option.hex) }
                                    .testTag("color_option_${option.hex}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(option.primary)
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(option.name.split(" ").firstOrNull() ?: "", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeColors.drop(4).take(4).forEach { option ->
                            val isSelected = option.hex.equals(activeThemeHex, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.setThemeColor(option.hex) }
                                    .testTag("color_option_${option.hex}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(option.primary)
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(option.name.split(" ").firstOrNull() ?: "", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Dark Mode & Display Options
            val currentThemeMode by viewModel.themeMode.collectAsState()
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "حالت شب و پوسته تاریک/روشن",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Text(
                        text = "می‌توانید هماهنگ با تنظیمات گوشی یا به صورت دستی انتخاب کنید:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentThemeMode == com.example.ui.ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(com.example.ui.ThemeMode.SYSTEM) },
                            leadingIcon = {
                                Icon(Icons.Default.SettingsBrightness, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("پیش‌فرض گوشی") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_system")
                        )

                        FilterChip(
                            selected = currentThemeMode == com.example.ui.ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(com.example.ui.ThemeMode.LIGHT) },
                            leadingIcon = {
                                Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("روشن") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_light")
                        )

                        FilterChip(
                            selected = currentThemeMode == com.example.ui.ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(com.example.ui.ThemeMode.DARK) },
                            leadingIcon = {
                                Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("تاریک") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_dark")
                        )
                    }
                }
            }

            // Financial & Invoice Defaults
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "تنظیمات پیش‌فرض مالی و صدور",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )

                    // Currency Selection
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("واحد پول پیش‌فرض", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("واحد پول فعلی: $currency", style = MaterialTheme.typography.bodySmall, color = themeColor)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Default Tax
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = taxInput,
                            onValueChange = {
                                taxInput = it
                                it.toDoubleOrNull()?.let { taxVal -> viewModel.setDefaultTaxPercent(taxVal) }
                            },
                            label = { Text("درصد مالیات پیش‌فرض") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = prefixInput,
                            onValueChange = {
                                prefixInput = it
                                viewModel.setInvoicePrefix(it)
                            },
                            label = { Text("پیش‌شماره فاکتور") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Links to Store & Bank
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "مدیریت حساب‌ها و فروشگاه",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.SellerSettings) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = themeColor)
                                Text("مشخصات فروشگاه و سربرگ فاکتور", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.BankAccounts) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = themeColor)
                                Text("مدیریت کارت‌ها و حساب‌های بانکی", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }

            // Database Reset Section
            var showResetDialog by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "مدیریت و پاک‌سازی داده‌ها",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "در صورت نیاز به شروع کار از ابتدا، می‌توانید تمام اطلاعات فاکتورها، مشتریان و کالاها را پاک کنید:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_reset_database")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("پاک‌سازی کامل پایگاه داده (شروع از صفر)")
                    }
                }
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
                    title = { Text("پاک‌سازی کامل داده‌ها") },
                    text = { Text("آیا مطمئن هستید؟ با این کار تمامی فاکتورها، کالاها، مشتریان و حساب‌های بانکی برای همیشه حذف خواهند شد و برنامه کاملاً خالی می‌شود.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.clearAllData {
                                    showResetDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("بله، همه را پاک کن")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("انصراف")
                        }
                    }
                )
            }

            // About App
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("نرم‌افزار فاکتور پلاس", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("نسخه ۱.۱۱ • مجهز به تقویم شمسی جلالی و پایگاه داده محلی سریع", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("انتخاب واحد پول") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("تومان", "ریال", "میلیون تومان").forEach { cur ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(cur)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cur, style = MaterialTheme.typography.bodyMedium)
                            RadioButton(
                                selected = currency == cur,
                                onClick = {
                                    viewModel.setCurrency(cur)
                                    showCurrencyDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }
}
