package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.utils.PersianNumberFormatter
import com.example.utils.ValidationUtil

@Composable
fun BatchPriceAdjustmentDialog(
    selectedCount: Int,
    currency: String,
    themeColor: Color,
    onDismissRequest: () -> Unit,
    onConfirm: (isPercentage: Boolean, amount: Double, isIncrease: Boolean) -> Unit
) {
    var isPercentage by remember { mutableStateOf(true) }
    var isIncrease by remember { mutableStateOf(true) }
    var inputValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_batch_price")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تغییر قیمت گروهی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = themeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$selectedCount کالا",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Tabs (Percentage vs Fixed Amount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isPercentage,
                        onClick = { isPercentage = true },
                        label = { Text("درصدی (٪)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor.copy(alpha = 0.15f),
                            selectedLabelColor = themeColor
                        )
                    )
                    FilterChip(
                        selected = !isPercentage,
                        onClick = { isPercentage = false },
                        label = { Text("مبلغ ثابت ($currency)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor.copy(alpha = 0.15f),
                            selectedLabelColor = themeColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direction: Increase vs Decrease
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isIncrease,
                        onClick = { isIncrease = true },
                        leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("افزایش قیمت") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF10B981)
                        )
                    )
                    FilterChip(
                        selected = !isIncrease,
                        onClick = { isIncrease = false },
                        leadingIcon = { Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("کاهش قیمت") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFFEF4444)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Value input
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        val cleaned = if (isPercentage) {
                            ValidationUtil.cleanDecimal(it)
                        } else {
                            ValidationUtil.cleanDigits(it)
                        }
                        inputValue = cleaned
                        isError = false
                    },
                    label = { Text(if (isPercentage) "میزان درصد (مثلا ۱۰ یا ۱۵.۵)" else "مبلغ ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = if (isPercentage) KeyboardType.Decimal else KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("لطفاً یک مقدار معتبر بزرگتر از صفر وارد کنید", color = MaterialTheme.colorScheme.error)
                        } else if (inputValue.isNotBlank() && !isPercentage) {
                            val num = inputValue.toLongOrNull() ?: 0L
                            Text(PersianNumberFormatter.formatPrice(num, currency))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_batch_price_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = {
                            val amount = inputValue.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) {
                                isError = true
                            } else {
                                onConfirm(isPercentage, amount, isIncrease)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_batch_price"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("اعمال تغییرات", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BatchCategoryChangeDialog(
    selectedCount: Int,
    existingCategories: List<String>,
    themeColor: Color,
    onDismissRequest: () -> Unit,
    onConfirm: (newCategory: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_batch_category")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "تغییر دسته‌بندی گروهی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "دسته‌بندی جدید برای $selectedCount کالای انتخاب‌شده را وارد یا انتخاب کنید:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Quick chips of existing categories
                val cleanCats = existingCategories.filter { it != "همه" && it.isNotBlank() }
                if (cleanCats.isNotEmpty()) {
                    Text(
                        text = "دسته‌های موجود:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cleanCats.take(4).forEach { cat ->
                            SuggestionChip(
                                onClick = {
                                    categoryName = cat
                                    isError = false
                                },
                                label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it.replace("\n", "").replace("\r", "")
                        isError = false
                    },
                    label = { Text("نام دسته‌بندی جدید *") },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("لطفاً نام دسته‌بندی را وارد کنید", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_batch_category_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = {
                            if (categoryName.isBlank()) {
                                isError = true
                            } else {
                                onConfirm(categoryName.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_batch_category"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ثبت و انتقال", color = Color.White)
                    }
                }
            }
        }
    }
}
