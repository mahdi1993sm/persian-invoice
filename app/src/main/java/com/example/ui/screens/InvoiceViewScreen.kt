package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.InvoiceWithItems
import com.example.ui.InvoiceViewModel
import com.example.ui.Screen
import com.example.ui.components.PdfActionType
import com.example.ui.components.PdfExportConfigDialog
import com.example.ui.components.StatusBadge
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.example.utils.ImageProcessingUtil
import com.example.utils.PdfGeneratorUtil
import com.example.utils.PersianNumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceViewScreen(
    invoiceId: Long,
    viewModel: InvoiceViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val sellerInfo by viewModel.sellerInfo.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeColorHex by viewModel.themeColorHex.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var invoiceData by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var paperSize by remember { mutableStateOf("A4") } // "A4" or "A5"
    var showSignatureArea by remember { mutableStateOf(true) }
    var showBankDetails by remember { mutableStateOf(true) }

    var pdfDialogActionType by remember { mutableStateOf<PdfActionType?>(null) }

    val signatureBitmap = remember(sellerInfo?.signatureImagePath) {
        sellerInfo?.signatureImagePath?.takeIf { it.isNotEmpty() }?.let {
            ImageProcessingUtil.loadBitmapFromFile(it)
        }
    }
    val stampBitmap = remember(sellerInfo?.stampImagePath) {
        sellerInfo?.stampImagePath?.takeIf { it.isNotEmpty() }?.let {
            ImageProcessingUtil.loadBitmapFromFile(it)
        }
    }

    LaunchedEffect(invoiceId) {
        invoiceData = viewModel.getInvoiceById(invoiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پیش‌نمایش فاکتور") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("invoice_view_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    // Quick Download PDF Button
                    IconButton(
                        onClick = { pdfDialogActionType = PdfActionType.DOWNLOAD },
                        modifier = Modifier.testTag("invoice_view_download_pdf_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "دانلود PDF روی گوشی", tint = themeColor)
                    }

                    // Share PDF Button
                    IconButton(
                        onClick = { pdfDialogActionType = PdfActionType.SHARE },
                        modifier = Modifier.testTag("invoice_view_share_pdf_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری فایل PDF", tint = themeColor)
                    }

                    // Edit Invoice Button
                    IconButton(
                        onClick = {
                            if (invoiceData != null) {
                                viewModel.navigateTo(Screen.InvoiceForm(invoiceId))
                            }
                        },
                        modifier = Modifier.testTag("invoice_view_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (invoiceData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColor)
            }
        } else {
            val item = invoiceData!!
            val inv = item.invoice

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("invoice_document_scrollable"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Formatting Controls Bar & Action Buttons
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Direct Download Button
                            Button(
                                onClick = { pdfDialogActionType = PdfActionType.DOWNLOAD },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("download_pdf_action_button")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("دانلود PDF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }

                            // Direct Share PDF Button
                            OutlinedButton(
                                onClick = { pdfDialogActionType = PdfActionType.SHARE },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("share_pdf_action_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = themeColor)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اشتراک PDF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("مهر/امضا:", style = MaterialTheme.typography.labelSmall)
                            Checkbox(
                                checked = showSignatureArea,
                                onCheckedChange = { showSignatureArea = it }
                            )
                        }
                    }
                }

                // Official Invoice Paper (White Sheet with Border)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    tonalElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .testTag("official_invoice_paper")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (paperSize == "A4") 18.dp else 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Header Title & Top Info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sellerInfo?.name ?: "فروشگاه",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "صورتحساب رسمی فروش کالا و خدمات",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF475569)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "شماره: ${inv.invoiceNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "تاریخ: ${inv.createdAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF475569)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "وضعیت: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                    StatusBadge(status = inv.status)
                                }
                            }
                        }

                        // 2. Seller and Buyer Information Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Seller Box
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "مشخصات فروشنده",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(sellerInfo?.name ?: "فروشگاه", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                                if (sellerInfo?.nationalCode?.isNotBlank() == true) {
                                    Text("شناسه ملی: ${sellerInfo!!.nationalCode}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                }
                                if (sellerInfo?.phone?.isNotBlank() == true) {
                                    Text("تلفن: ${sellerInfo!!.phone}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                }
                                if (sellerInfo?.address?.isNotBlank() == true) {
                                    Text("آدرس: ${sellerInfo!!.address}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                }
                            }

                            // Buyer Box
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "مشخصات خریدار",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(inv.customerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("نوع تسویه: ${if (inv.paymentType == "cash") "نقدی" else if (inv.paymentType == "credit") "نسیه (اعتباری)" else "چک صیادی"}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                if (inv.dueDate.isNotBlank()) {
                                    Text("سررسید: ${inv.dueDate}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                }
                            }
                        }

                        // 3. Items Table
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp))
                        ) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9))
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ردیف", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.width(32.dp))
                                Text("شرح کالا یا خدمات", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
                                Text("تعداد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                                Text("قیمت واحد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                Text("مبلغ کل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                            }

                            Divider(color = Color(0xFF94A3B8))

                            // Table Content Rows
                            item.items.forEachIndexed { idx, itm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (idx % 2 == 0) Color.White else Color(0xFFFAFAFA))
                                    .padding(vertical = 6.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${idx + 1}", style = MaterialTheme.typography.bodySmall, color = Color.Black, modifier = Modifier.width(32.dp))
                                    Text(itm.productName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.Black, modifier = Modifier.weight(1f))
                                    Text("${itm.quantity} ${itm.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Black, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                                    Text(PersianNumberFormatter.formatNumber(itm.unitPrice), style = MaterialTheme.typography.bodySmall, color = Color.Black, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    Text(PersianNumberFormatter.formatNumber(itm.totalRowPrice), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                                }
                                if (idx < item.items.size - 1) {
                                    Divider(color = Color(0xFFE2E8F0))
                                }
                            }
                        }

                        // 4. Financial Calculations Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("جمع کل اقلام:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                Text(PersianNumberFormatter.formatPrice(inv.subtotal, currency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            if (inv.discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("تخفیف کلی (${inv.discountPercent}%):", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                                    Text("- " + PersianNumberFormatter.formatPrice(inv.discountAmount, currency), style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                            }

                            if (inv.taxAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("مالیات و عوارض ارزش افزوده (${inv.taxPercent}%):", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                    Text("+ " + PersianNumberFormatter.formatPrice(inv.taxAmount, currency), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            Divider(color = Color(0xFFCBD5E1), modifier = Modifier.padding(vertical = 2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("مبلغ کل نهایی قابل پرداخت:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(
                                    text = PersianNumberFormatter.formatPrice(inv.totalAmount, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }

                            if (inv.paidAmount > 0 || inv.remainingAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("مبلغ دریافتی:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                                    Text(PersianNumberFormatter.formatPrice(inv.paidAmount, currency), style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                            }

                            if (inv.remainingAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("مانده بدهی (تسویه نشده):", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF4444))
                                    Text(PersianNumberFormatter.formatPrice(inv.remainingAmount, currency), style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 5. Payment & Financial Conditions
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("شرایط مالی و وضعیت پرداخت:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeColor)
                                
                                val payDateStr = if (inv.paymentDate.isNotBlank()) inv.paymentDate else inv.createdAt
                                val payAmountStr = if (inv.paidAmount > 0) PersianNumberFormatter.formatPrice(inv.paidAmount, currency) else "—"
                                Text("مبلغ پرداختی: $payAmountStr  •  تاریخ پرداخت: $payDateStr", style = MaterialTheme.typography.bodySmall, color = Color(0xFF334155))

                                val methodStr = inv.paymentMethod.ifBlank {
                                    when (inv.paymentType) {
                                        "cash" -> when (inv.cashPaymentMethod) {
                                            "pos" -> "کارتخوان (پوز)"
                                            "card_to_card" -> "کارت به کارت"
                                            "paya" -> "پایا / ساتنا"
                                            else -> "نقدی"
                                        }
                                        "credit" -> "نسیه"
                                        "check" -> "چک صیادی"
                                        else -> inv.paymentType
                                    }
                                }
                                val accStr = if (inv.paymentAccountInfo.isNotBlank()) inv.paymentAccountInfo else if (inv.bankAccountName.isNotBlank()) inv.bankAccountName else "—"
                                Text("روش پرداخت: $methodStr  •  به حساب: $accStr", style = MaterialTheme.typography.bodySmall, color = Color(0xFF334155))

                                if (inv.paymentTrackingCode.isNotBlank()) {
                                    Text("کد رهگیری تراکنش: ${inv.paymentTrackingCode}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                }

                                if (inv.remainingAmount > 0) {
                                    val dueStr = if (inv.dueDate.isNotBlank()) " (سررسید: ${inv.dueDate})" else ""
                                    Text("مانده بدهی نسیه: ${PersianNumberFormatter.formatPrice(inv.remainingAmount, currency)}$dueStr", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                }

                                if (inv.paymentType == "check" && inv.checkSayadiId.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("مشخصات چک صیادی:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                    Text("شناسه صیادی: ${inv.checkSayadiId} • بانک: ${inv.checkBankName} • شماره چک: ${inv.checkNumber}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                    Text("سررسید چک: ${inv.checkDueDate} • نام صادرکننده: ${inv.checkIssuer}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                }
                            }
                        }

                        // 6. Footer Terms & Conditions
                        val footerText = inv.description.ifBlank { sellerInfo?.footerText ?: "" }
                        if (footerText.isNotBlank()) {
                            Text(
                                text = "شرایط: $footerText",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }

                        // 7. Signature & Stamp Area
                        if (showSignatureArea) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("مهر و امضای خریدار", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                                    Text("(تأیید صحت اقلام)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color(0xFF94A3B8))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("مهر و امضای فروشنده", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (signatureBitmap != null || stampBitmap != null) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (stampBitmap != null) {
                                                    Image(
                                                        bitmap = stampBitmap.asImageBitmap(),
                                                        contentDescription = "مهر فروشگاه",
                                                        modifier = Modifier
                                                            .size(52.dp)
                                                            .padding(2.dp)
                                                    )
                                                }
                                                if (signatureBitmap != null) {
                                                    Image(
                                                        bitmap = signatureBitmap.asImageBitmap(),
                                                        contentDescription = "امضای فروشنده",
                                                        modifier = Modifier
                                                            .height(48.dp)
                                                            .widthIn(max = 84.dp)
                                                            .padding(2.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Text(
                                                sellerInfo?.signatureName ?: "امضا و مهر",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColor
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

    // PDF Configuration Dialog for Download OR Share
    if (pdfDialogActionType != null && invoiceData != null) {
        val target = invoiceData!!
        val action = pdfDialogActionType!!

        PdfExportConfigDialog(
            invoiceNumber = target.invoice.invoiceNumber,
            themeColor = themeColor,
            actionType = action,
            onDismissRequest = { pdfDialogActionType = null },
            onConfirmExport = { config ->
                try {
                    if (action == PdfActionType.DOWNLOAD) {
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
                    } else {
                        PdfGeneratorUtil.sharePdfFile(
                            context = context,
                            invoiceWithItems = target,
                            sellerInfo = sellerInfo,
                            currency = currency,
                            themeColorHex = themeColorHex,
                            config = config
                        )
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "خطا در عملیات PDF: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    pdfDialogActionType = null
                }
            }
        )
    }
}
