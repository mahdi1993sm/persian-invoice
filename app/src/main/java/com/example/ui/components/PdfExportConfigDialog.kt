package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.utils.PdfExportConfig

enum class PdfActionType {
    DOWNLOAD,
    SHARE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportConfigDialog(
    invoiceNumber: String,
    themeColor: Color,
    actionType: PdfActionType = PdfActionType.DOWNLOAD,
    onDismissRequest: () -> Unit,
    onConfirmExport: (PdfExportConfig) -> Unit
) {
    var includeSignature by remember { mutableStateOf(true) }
    var paperSize by remember { mutableStateOf("A4") } // "A4" or "A5"
    var isLandscape by remember { mutableStateOf(false) } // false = Portrait, true = Landscape

    val isDownload = actionType == PdfActionType.DOWNLOAD
    val dialogTitle = if (isDownload) "تنظیمات دانلود PDF روی گوشی" else "تنظیمات اشتراک‌گذاری PDF"
    val actionButtonText = if (isDownload) "دانلود روی گوشی" else "اشتراک‌گذاری PDF"
    val actionIcon = if (isDownload) Icons.Default.FileDownload else Icons.Default.Share

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("pdf_export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(themeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDownload) Icons.Default.PictureAsPdf else Icons.Default.Share,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = dialogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "فاکتور $invoiceNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Question 1: Stamp & Signature Toggle
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "۱. ثبت مهر و امضا در انتهای فاکتور:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = includeSignature,
                            onClick = { includeSignature = true },
                            leadingIcon = {
                                if (includeSignature) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            label = { Text("با مهر و امضا") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("option_signature_yes")
                        )

                        FilterChip(
                            selected = !includeSignature,
                            onClick = { includeSignature = false },
                            leadingIcon = {
                                if (!includeSignature) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            label = { Text("بدون مهر و امضا") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("option_signature_no")
                        )
                    }
                }

                // Question 2: Paper Size (A4 or A5)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "۲. ابعاد کاغذ فاکتور:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = paperSize == "A4",
                            onClick = { paperSize = "A4" },
                            leadingIcon = {
                                if (paperSize == "A4") {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            label = { Text("کاغذ A4") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("option_size_a4")
                        )

                        FilterChip(
                            selected = paperSize == "A5",
                            onClick = { paperSize = "A5" },
                            leadingIcon = {
                                if (paperSize == "A5") {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            label = { Text("کاغذ A5") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("option_size_a5")
                        )
                    }
                }

                // Question 3: Orientation (Portrait or Landscape)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "۳. جهت برگه:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isLandscape,
                            onClick = { isLandscape = false },
                            leadingIcon = {
                                Icon(Icons.Default.StayCurrentPortrait, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("عمودی") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("option_orientation_portrait")
                        )

                        FilterChip(
                            selected = isLandscape,
                            onClick = { isLandscape = true },
                            leadingIcon = {
                                Icon(Icons.Default.StayCurrentLandscape, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("افقی") },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = themeColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("option_orientation_landscape")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text("انصراف")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val config = PdfExportConfig(
                                includeSignatureAndStamp = includeSignature,
                                paperSize = paperSize,
                                isLandscape = isLandscape
                            )
                            onConfirmExport(config)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_confirm_pdf_button")
                    ) {
                        Icon(actionIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(actionButtonText)
                    }
                }
            }
        }
    }
}
