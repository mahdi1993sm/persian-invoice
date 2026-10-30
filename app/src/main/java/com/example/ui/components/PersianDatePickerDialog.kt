package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.utils.JalaliCalendar
import com.example.utils.JalaliDate

@Composable
fun PersianDatePickerDialog(
    initialDate: String? = null,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val initial = remember(initialDate) {
        if (!initialDate.isNullOrBlank()) {
            JalaliCalendar.parse(initialDate) ?: JalaliCalendar.getToday()
        } else {
            JalaliCalendar.getToday()
        }
    }

    var selectedYear by remember { mutableIntStateOf(initial.year) }
    var selectedMonth by remember { mutableIntStateOf(initial.month) }
    var selectedDay by remember { mutableIntStateOf(initial.day) }

    val daysInCurrentMonth = remember(selectedYear, selectedMonth) {
        JalaliCalendar.daysInMonth(selectedYear, selectedMonth)
    }

    // Clamp selectedDay if month changed
    LaunchedEffect(daysInCurrentMonth) {
        if (selectedDay > daysInCurrentMonth) {
            selectedDay = daysInCurrentMonth
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
                .testTag("persian_date_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Display
                val monthName = JalaliCalendar.MONTH_NAMES.getOrElse(selectedMonth - 1) { "" }
                Text(
                    text = "انتخاب تاریخ شمسی",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$selectedDay $monthName $selectedYear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Month / Year Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedMonth > 1) {
                                selectedMonth--
                            } else {
                                selectedMonth = 12
                                selectedYear--
                            }
                        },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ماه قبل"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$selectedYear",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            if (selectedMonth < 12) {
                                selectedMonth++
                            } else {
                                selectedMonth = 1
                                selectedYear++
                            }
                        },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "ماه بعد"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days Grid
                val daysList = (1..daysInCurrentMonth).toList()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysList) { day ->
                        val isSelected = (day == selectedDay)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable {
                                    selectedDay = day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$day",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val today = JalaliCalendar.getToday()
                            selectedYear = today.year
                            selectedMonth = today.month
                            selectedDay = today.day
                        },
                        modifier = Modifier.testTag("today_button")
                    ) {
                        Text("امروز")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.testTag("cancel_date_button")
                        ) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val formatted = String.format("%04d/%02d/%02d", selectedYear, selectedMonth, selectedDay)
                                onDateSelected(formatted)
                                onDismissRequest()
                            },
                            modifier = Modifier.testTag("confirm_date_button")
                        ) {
                            Text("تأیید")
                        }
                    }
                }
            }
        }
    }
}
