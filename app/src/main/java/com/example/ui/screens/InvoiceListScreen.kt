package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import com.example.data.db.InvoiceWithItems
import com.example.data.model.Invoice
import com.example.ui.InvoiceViewModel
import com.example.ui.Screen
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PdfActionType
import com.example.ui.components.PdfExportConfigDialog
import com.example.ui.components.SearchInputField
import com.example.ui.components.StatusBadge
import com.example.utils.PdfGeneratorUtil
import com.example.utils.PersianNumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(viewModel: InvoiceViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val allInvoices by viewModel.invoicesWithItems.collectAsState()
    val sellerInfo by viewModel.sellerInfo.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeColorHex by viewModel.themeColorHex.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var searchQuery by remember { mutableStateOf("") }
    val currentFilterTab by viewModel.invoiceFilterTab.collectAsState()
    var selectedFilterTab by remember(currentFilterTab) { mutableIntStateOf(currentFilterTab) }
    var invoiceToDelete by remember { mutableStateOf<Invoice?>(null) }
    var invoiceForPdf by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var pdfActionType by remember { mutableStateOf(PdfActionType.DOWNLOAD) }

    val filterTabs = listOf("همه", "تسویه شده", "نسیه", "چک / اقساط")

    val filteredInvoices = remember(allInvoices, searchQuery, selectedFilterTab) {
        allInvoices.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.invoice.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    item.invoice.customerName.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedFilterTab) {
                1 -> item.invoice.status == "paid"
                2 -> item.invoice.status == "credit"
                3 -> item.invoice.paymentType == "check"
                else -> true
            }

            matchesSearch && matchesTab
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.InvoiceForm(null)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("صدور فاکتور جدید") },
                containerColor = themeColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_invoice")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .testTag("invoice_list_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            SearchInputField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "جستجو در شماره فاکتور یا نام خریدار..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterTabs.indices.toList()) { index ->
                    val isSelected = selectedFilterTab == index
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFilterTab = index
                            viewModel.setInvoiceFilterTab(index)
                        },
                        label = { Text(filterTabs[index]) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor.copy(alpha = 0.15f),
                            selectedLabelColor = themeColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary count & total
            val filteredTotal = remember(filteredInvoices) { filteredInvoices.sumOf { it.invoice.totalAmount } }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredInvoices.size} فاکتور یافت شد",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "مجموع: " + PersianNumberFormatter.formatPrice(filteredTotal, currency),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredInvoices.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Receipt,
                    title = "فاکتوری یافت نشد",
                    description = if (searchQuery.isNotEmpty()) "عبارت جستجو شده با فاکتورهای ثبت شده همخوانی ندارد."
                    else "هیچ فاکتوری در این دسته‌بندی وجود ندارد.",
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
                    items(filteredInvoices, key = { it.invoice.id }) { item ->
                        InvoiceListItemCard(
                            item = item,
                            currency = currency,
                            themeColor = themeColor,
                            onView = { viewModel.navigateTo(Screen.InvoiceView(item.invoice.id)) },
                            onEdit = { viewModel.navigateTo(Screen.InvoiceForm(item.invoice.id)) },
                            onDelete = { invoiceToDelete = item.invoice },
                            onDownloadPdf = {
                                invoiceForPdf = item
                                pdfActionType = PdfActionType.DOWNLOAD
                            },
                            onSharePdf = {
                                invoiceForPdf = item
                                pdfActionType = PdfActionType.SHARE
                            }
                        )
                    }
                }
            }
        }
    }

    // PDF Configuration Dialog for Download OR Share
    if (invoiceForPdf != null) {
        val target = invoiceForPdf!!
        PdfExportConfigDialog(
            invoiceNumber = target.invoice.invoiceNumber,
            themeColor = themeColor,
            actionType = pdfActionType,
            onDismissRequest = { invoiceForPdf = null },
            onConfirmExport = { config ->
                try {
                    if (pdfActionType == PdfActionType.DOWNLOAD) {
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
                    invoiceForPdf = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (invoiceToDelete != null) {
        val target = invoiceToDelete!!
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("حذف فاکتور") },
            text = { Text("آیا از حذف فاکتور ${target.invoiceNumber} مربوط به «${target.customerName}» اطمینان دارید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(target)
                        invoiceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف فاکتور")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { invoiceToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun InvoiceListItemCard(
    item: InvoiceWithItems,
    currency: String,
    themeColor: Color,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("invoice_card_${item.invoice.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Number + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.invoice.invoiceNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(status = item.invoice.status)
                }

                Text(
                    text = item.invoice.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = item.invoice.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items Count and Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.items.size} قلم کالا • نوع تسویه: ${
                        when (item.invoice.paymentType) {
                            "cash" -> "نقدی"
                            "credit" -> "نسیه"
                            "check" -> "چک صیادی"
                            else -> item.invoice.paymentType
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = PersianNumberFormatter.formatPrice(item.invoice.totalAmount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
            }

            if (item.invoice.remainingAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "مانده بدهی: " + PersianNumberFormatter.formatPrice(item.invoice.remainingAmount, currency),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Action Buttons Row with Distinct Download and Share Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // PDF Download Shortcut Button
                    FilledTonalButton(
                        onClick = onDownloadPdf,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = themeColor.copy(alpha = 0.15f),
                            contentColor = themeColor
                        ),
                        modifier = Modifier.testTag("invoice_pdf_download_action_${item.invoice.id}")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("دانلود PDF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = onView,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("invoice_view_action_${item.invoice.id}")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("مشاهده", style = MaterialTheme.typography.labelSmall)
                    }

                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("invoice_edit_action_${item.invoice.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("ویرایش", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // PDF Share Button
                    IconButton(
                        onClick = onSharePdf,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("invoice_pdf_share_action_${item.invoice.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "اشتراک‌گذاری PDF",
                            tint = themeColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
