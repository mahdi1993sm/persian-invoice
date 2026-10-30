package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.utils.ImageProcessingUtil

@Composable
fun BackgroundRemovalDialog(
    originalBitmap: Bitmap,
    title: String,
    themeColor: Color,
    onDismissRequest: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    var threshold by remember { mutableFloatStateOf(0.80f) }
    var enhanceContrast by remember { mutableStateOf(true) }

    val processedBitmap by remember(originalBitmap, threshold, enhanceContrast) {
        derivedStateOf {
            ImageProcessingUtil.removeBackground(
                source = originalBitmap,
                threshold = threshold,
                enhanceContrast = enhanceContrast
            )
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("bg_removal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = themeColor
                    )
                    Text(
                        text = "حذف هوشمند پس‌زمینه $title",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "برنامه به صورت خودکار و ۱۰۰٪ آفلاین زمینه کاغذی سفید/خاکستری را حذف نموده و تصویری شفاف (PNG) می‌سازد:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Checkerboard Preview Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    // Draw checkerboard transparency pattern
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val squareSize = 16f
                        val cols = (size.width / squareSize).toInt() + 1
                        val rows = (size.height / squareSize).toInt() + 1
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val color = if ((r + c) % 2 == 0) Color(0xFFF1F5F9) else Color(0xFFCBD5E1)
                                drawRect(
                                    color = color,
                                    topLeft = Offset(c * squareSize, r * squareSize),
                                    size = Size(squareSize, squareSize)
                                )
                            }
                        }
                    }

                    // Render processed transparent bitmap
                    Image(
                        bitmap = processedBitmap.asImageBitmap(),
                        contentDescription = "پیش‌نمایش تصویر شفاف",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }

                // Sensitivity Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "حساسیت حذف زمینه کاغذ:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(threshold * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    }

                    Slider(
                        value = threshold,
                        onValueChange = { threshold = it },
                        valueRange = 0.50f..0.96f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColor,
                            activeTrackColor = themeColor
                        ),
                        modifier = Modifier.testTag("sensitivity_slider")
                    )
                }

                // Enhance ink contrast toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پررنگ‌سازی و تقویت رنگ جوهر / مهر",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = enhanceContrast,
                        onCheckedChange = { enhanceContrast = it }
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(processedBitmap) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_bg_removal_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تأیید و ذخیره")
                    }
                }
            }
        }
    }
}
