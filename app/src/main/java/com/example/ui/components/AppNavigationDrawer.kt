package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SellerInfo
import com.example.ui.Screen

@Composable
fun AppDrawerContent(
    currentScreen: Screen,
    sellerInfo: SellerInfo?,
    themeColor: Color,
    onNavigate: (Screen) -> Unit,
    onOpenAboutUs: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .widthIn(max = 320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                themeColor,
                                themeColor.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                        Column {
                            Text(
                                text = sellerInfo?.name?.ifBlank { null } ?: "نرم‌افزار صدور فاکتور",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (!sellerInfo?.phone.isNullOrBlank()) "تلفن: ${sellerInfo?.phone}" else "مدیریت یکپارچه فروش",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Section Label
            Text(
                text = "بخش‌های نرم‌افزار",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Items
            DrawerNavigationItem(
                label = "داشبورد",
                icon = Icons.Default.Dashboard,
                selected = currentScreen is Screen.Dashboard,
                onClick = { onNavigate(Screen.Dashboard) },
                testTag = "drawer_item_dashboard",
                themeColor = themeColor
            )

            DrawerNavigationItem(
                label = "فاکتورهای فروش",
                icon = Icons.Default.ReceiptLong,
                selected = currentScreen is Screen.InvoiceList,
                onClick = { onNavigate(Screen.InvoiceList) },
                testTag = "drawer_item_invoices",
                themeColor = themeColor
            )

            DrawerNavigationItem(
                label = "صدور فاکتور جدید",
                icon = Icons.Default.AddCircleOutline,
                selected = currentScreen is Screen.InvoiceForm,
                onClick = { onNavigate(Screen.InvoiceForm(null)) },
                testTag = "drawer_item_new_invoice",
                themeColor = themeColor
            )

            DrawerNavigationItem(
                label = "کالاها و انبار",
                icon = Icons.Default.Inventory2,
                selected = currentScreen is Screen.Products,
                onClick = { onNavigate(Screen.Products) },
                testTag = "drawer_item_products",
                themeColor = themeColor
            )

            DrawerNavigationItem(
                label = "طرف‌های حساب و مشتریان",
                icon = Icons.Default.People,
                selected = currentScreen is Screen.Customers,
                onClick = { onNavigate(Screen.Customers) },
                testTag = "drawer_item_customers",
                themeColor = themeColor
            )

            DrawerNavigationItem(
                label = "حساب‌های بانکی",
                icon = Icons.Default.AccountBalance,
                selected = currentScreen is Screen.BankAccounts,
                onClick = { onNavigate(Screen.BankAccounts) },
                testTag = "drawer_item_bank_accounts",
                themeColor = themeColor
            )

            DrawerNavigationItem(
                label = "مشخصات و مهر فروشنده",
                icon = Icons.Default.Storefront,
                selected = currentScreen is Screen.SellerSettings,
                onClick = { onNavigate(Screen.SellerSettings) },
                testTag = "drawer_item_seller_settings",
                themeColor = themeColor
            )

            DrawerNavigationItem(
                label = "تنظیمات و قالب",
                icon = Icons.Default.Tune,
                selected = currentScreen is Screen.Settings,
                onClick = { onNavigate(Screen.Settings) },
                testTag = "drawer_item_settings",
                themeColor = themeColor
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // About Us Item
            DrawerNavigationItem(
                label = "درباره ما و راهنما",
                icon = Icons.Default.Info,
                selected = false,
                onClick = onOpenAboutUs,
                testTag = "drawer_item_about_us",
                themeColor = themeColor
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "فاکتور ساز هوشمند • نسخه ۱.۴.۰",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun DrawerNavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    themeColor: Color
) {
    val containerColor = if (selected) themeColor.copy(alpha = 0.14f) else Color.Transparent
    val contentColor = if (selected) themeColor else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(themeColor)
                )
            }
        }
    }
}
