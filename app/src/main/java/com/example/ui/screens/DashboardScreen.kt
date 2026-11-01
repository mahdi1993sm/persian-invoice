package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.InvoiceViewModel
import com.example.ui.Screen
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.utils.JalaliCalendar
import com.example.utils.PersianNumberFormatter

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.data.db.InvoiceWithItems
import com.example.ui.components.PdfExportConfigDialog
import com.example.utils.PdfGeneratorUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: InvoiceViewModel) {
    val context = LocalContext.current
    val invoices by viewModel.invoicesWithItems.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()
    val sellerInfo by viewModel.sellerInfo.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeColorHex by viewModel.themeColorHex.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var invoiceForPdfExport by remember { mutableStateOf<InvoiceWithItems?>(null) }

    // Calculated metrics
    val totalSales = remember(invoices) { invoices.sumOf { it.invoice.totalAmount } }
    val totalPaid = remember(invoices) { invoices.sumOf { it.invoice.paidAmount } }
    val totalCredit = remember(invoices) { invoices.sumOf { it.invoice.remainingAmount } }
    val paidCount = remember(invoices) { invoices.count { it.invoice.status == "paid" } }
    val creditCount = remember(invoices) { invoices.count { it.invoice.status == "credit" } }

    // Monthly breakdown for current Jalali year
    val monthlySales = remember(invoices) {
        val months = LongArray(12) { 0L }
        invoices.forEach { item ->
            val parsed = JalaliCalendar.parse(item.invoice.createdAt)
            if (parsed != null && parsed.month in 1..12) {
                months[parsed.month - 1] += item.invoice.totalAmount
            }
        }
        months
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = themeColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "سیستم هوشمند صدور فاکتور",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "امروز: ${JalaliCalendar.getToday().format(includeMonthName = true)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Text(
                text = "دسترسی سریع",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.InvoiceForm(null)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("new_invoice_quick_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "فاکتور جدید",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.Customers) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("customers_quick_button")
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مشتریان",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.Products) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("products_quick_button")
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "کالاها",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Financial Stats Grid (2x2) - Clickable to respective reports
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "مجموع فروش",
                        value = PersianNumberFormatter.formatPrice(totalSales, currency),
                        subtitle = "${invoices.size} فاکتور ثبت‌شده",
                        icon = Icons.Default.TrendingUp,
                        accentColor = themeColor,
                        onClick = { viewModel.navigateToInvoiceList(0) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_sales")
                    )
                    StatCard(
                        title = "دریافتی (تسویه)",
                        value = PersianNumberFormatter.formatPrice(totalPaid, currency),
                        subtitle = "$paidCount فاکتور تسویه‌شده",
                        icon = Icons.Default.CheckCircle,
                        accentColor = Color(0xFF10B981),
                        onClick = { viewModel.navigateToInvoiceList(1) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_paid")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "مطالبات (نسیه)",
                        value = PersianNumberFormatter.formatPrice(totalCredit, currency),
                        subtitle = "$creditCount فاکتور دارای مانده",
                        icon = Icons.Default.HourglassEmpty,
                        accentColor = Color(0xFFEF4444),
                        onClick = { viewModel.navigateToInvoiceList(2) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_credit")
                    )
                    StatCard(
                        title = "تعداد مشتریان",
                        value = "${customers.size} مشتری",
                        subtitle = "${products.size} قلم کالا در انبار",
                        icon = Icons.Default.Storefront,
                        accentColor = Color(0xFF3B82F6),
                        onClick = { viewModel.navigateTo(Screen.Customers) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_customer_count")
                    )
                }
            }
        }

        // Monthly Sales Jalali Chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_sales_chart_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نمودار فروش ماهانه (سال جاری)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "واحد: $currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Canvas Bar Chart for 12 Persian Months
                    val maxMonthVal = remember(monthlySales) {
                        val max = monthlySales.maxOrNull() ?: 0L
                        if (max > 0) max else 10000000L
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = size.width / 14f
                            val spacing = barWidth * 0.16f
                            val maxH = size.height - 24f

                            for (i in 0 until 12) {
                                val x = spacing + i * (barWidth + spacing)
                                val ratio = (monthlySales[i].toFloat() / maxMonthVal.toFloat()).coerceIn(0.04f, 1f)
                                val barH = maxH * ratio
                                val y = maxH - barH

                                // Bar
                                drawRoundRect(
                                    color = if (monthlySales[i] > 0) themeColor else Color(0xFFE2E8F0),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }

                    // Month Labels Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val shortMonths = listOf("فر", "ارد", "خرد", "تیر", "مرد", "شهر", "مهر", "آبا", "آذر", "دی", "بهم", "اسف")
                        shortMonths.forEach { m ->
                            Text(
                                text = m,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Recent Invoices Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخرین فاکتورهای صادره",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { viewModel.navigateTo(Screen.InvoiceList) },
                    modifier = Modifier.testTag("view_all_invoices_button")
                ) {
                    Text("مشاهده همه (${invoices.size})")
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (invoices.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "هنوز فاکتوری صادر نشده است. با زدن دکمه «فاکتور جدید» اولین فاکتور را صادر کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(invoices.take(4), key = { it.invoice.id }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.InvoiceView(item.invoice.id)) }
                        .testTag("recent_invoice_${item.invoice.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.invoice.invoiceNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusBadge(status = item.invoice.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.invoice.customerName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "تاریخ: ${item.invoice.createdAt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = PersianNumberFormatter.formatPrice(item.invoice.totalAmount, currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                                if (item.invoice.remainingAmount > 0) {
                                    Text(
                                        text = "مانده: " + PersianNumberFormatter.formatPrice(item.invoice.remainingAmount, currency),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { invoiceForPdfExport = item },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("dashboard_pdf_shortcut_${item.invoice.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "دانلود PDF",
                                    tint = themeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // PDF Configuration Dialog (Download)
    if (invoiceForPdfExport != null) {
        val target = invoiceForPdfExport!!
        PdfExportConfigDialog(
            invoiceNumber = target.invoice.invoiceNumber,
            themeColor = themeColor,
            onDismissRequest = { invoiceForPdfExport = null },
            onConfirmExport = { config ->
                try {
                    PdfGeneratorUtil.downloadPdfToDevice(
                        context = context,
                        invoiceWithItems = target,
                        sellerInfo = sellerInfo,
                        currency = currency,
                        themeColorHex = themeColorHex,
                        config = config
                    )
                    Toast.makeText(
                        context,
                        "فایل PDF فاکتور با موفقیت در پوشه دانلودها ذخیره شد",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "خطا در دانلود فایل PDF: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    invoiceForPdfExport = null
                }
            }
        )
    }
}
