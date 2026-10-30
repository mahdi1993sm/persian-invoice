package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.InvoiceViewModel
import com.example.ui.components.BatchCategoryChangeDialog
import com.example.ui.components.BatchPriceAdjustmentDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchInputField
import com.example.utils.PersianNumberFormatter
import com.example.utils.ValidationUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: InvoiceViewModel) {
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateBack()
    }

    val products by viewModel.products.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("همه") }
    var isGridView by remember { mutableStateOf(false) }

    // Batch Operations State
    var isBatchMode by remember { mutableStateOf(false) }
    var selectedProductIds by remember { mutableStateOf(setOf<Long>()) }
    var showBatchPriceDialog by remember { mutableStateOf(false) }
    var showBatchCategoryDialog by remember { mutableStateOf(false) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Categories
    val categories = remember(products) {
        val set = mutableSetOf("همه")
        products.forEach { if (it.category.isNotBlank()) set.add(it.category) }
        set.toList()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchesSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.code.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "همه" || prod.category == selectedCategory

            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        floatingActionButton = {
            if (!isBatchMode) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                    text = { Text("کالای جدید") },
                    containerColor = themeColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_product")
                )
            }
        },
        bottomBar = {
            // Batch Action Bar when items are selected
            AnimatedVisibility(visible = isBatchMode && selectedProductIds.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedProductIds.size} کالا انتخاب شده",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                            TextButton(onClick = { selectedProductIds = emptySet() }) {
                                Text("لغو انتخاب‌ها", color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showBatchPriceDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_batch_price")
                            ) {
                                Icon(Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تغییر قیمت", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { showBatchCategoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_batch_category")
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تغییر دسته", style = MaterialTheme.typography.labelMedium)
                            }

                            FilledTonalButton(
                                onClick = { showBatchDeleteConfirm = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                                    contentColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_batch_delete")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف گروهی", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .testTag("products_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchInputField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "جستجوی نام یا کد کالا...",
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.testTag("toggle_product_view")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "تغییر حالت نمایش",
                        tint = themeColor
                    )
                }

                FilledTonalButton(
                    onClick = {
                        isBatchMode = !isBatchMode
                        if (!isBatchMode) selectedProductIds = emptySet()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isBatchMode) themeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isBatchMode) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.testTag("toggle_batch_mode")
                ) {
                    Icon(
                        imageVector = if (isBatchMode) Icons.Default.ChecklistRtl else Icons.Default.Checklist,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBatchMode) "اتمام" else "ویرایش گروهی",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Batch selection control row (Select All / Invert)
            if (isBatchMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "کالاهای مورد نظر را علامت بزنید:",
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColor,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            selectedProductIds = if (selectedProductIds.size == filteredProducts.size) {
                                emptySet()
                            } else {
                                filteredProducts.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedProductIds.size == filteredProducts.size) "لغو همه" else "انتخاب همه (${filteredProducts.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor.copy(alpha = 0.15f),
                            selectedLabelColor = themeColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${filteredProducts.size} کالا در انبار",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Inventory2,
                    title = "کالایی یافت نشد",
                    description = if (searchQuery.isNotEmpty()) "کالایی با این نام یا کد وجود ندارد."
                    else "هنوز کالایی ثبت نشده است. با دکمه «کالای جدید» اضافه کنید.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                if (isGridView) {
                    // Grid View
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = if (isBatchMode && selectedProductIds.isNotEmpty()) 120.dp else 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { prod ->
                            val isSelected = prod.id in selectedProductIds
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) themeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isBatchMode) {
                                            selectedProductIds = if (isSelected) selectedProductIds - prod.id else selectedProductIds + prod.id
                                        } else {
                                            productToEdit = prod
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(themeColor.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Inventory, contentDescription = null, tint = themeColor, modifier = Modifier.size(20.dp))
                                        }
                                        if (isBatchMode) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    selectedProductIds = if (checked) selectedProductIds + prod.id else selectedProductIds - prod.id
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    if (prod.category.isNotBlank()) {
                                        Text(
                                            text = prod.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = PersianNumberFormatter.formatPrice(prod.price, currency),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColor
                                    )
                                    Text(
                                        text = "واحد: ${prod.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // List View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = if (isBatchMode && selectedProductIds.isNotEmpty()) 120.dp else 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { prod ->
                            val isSelected = prod.id in selectedProductIds
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) themeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isBatchMode) {
                                            selectedProductIds = if (isSelected) selectedProductIds - prod.id else selectedProductIds + prod.id
                                        }
                                    }
                                    .testTag("product_card_${prod.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (isBatchMode) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    selectedProductIds = if (checked) selectedProductIds + prod.id else selectedProductIds - prod.id
                                                }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(themeColor.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Category, contentDescription = null, tint = themeColor, modifier = Modifier.size(22.dp))
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = prod.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (prod.code.isNotBlank()) {
                                                    Text("کد: ${prod.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Text("• دسته: ${prod.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(
                                                text = "${PersianNumberFormatter.formatPrice(prod.price, currency)} / هر ${prod.unit}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColor
                                            )
                                        }
                                    }

                                    if (!isBatchMode) {
                                        Row {
                                            IconButton(
                                                onClick = { productToEdit = prod },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { productToDelete = prod },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
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

    // Add / Edit Product Dialog
    if (showAddDialog || productToEdit != null) {
        val editing = productToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var code by remember { mutableStateOf(editing?.code ?: "PRD-${System.currentTimeMillis() % 1000}") }
        var unit by remember { mutableStateOf(editing?.unit ?: "عدد") }
        var priceInput by remember { mutableStateOf(editing?.price?.toString() ?: "") }
        var category by remember { mutableStateOf(editing?.category ?: "عمومی") }
        var description by remember { mutableStateOf(editing?.description ?: "") }
        var error by remember { mutableStateOf<String?>(null) }
        var nameError by remember { mutableStateOf(false) }
        var priceError by remember { mutableStateOf(false) }

        val dialogScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        val commonUnits = listOf("عدد", "دستگاه", "بسته", "کارتن", "متر", "کیلوگرم", "حلقه", "ساعت")

        Dialog(
            onDismissRequest = {
                showAddDialog = false
                productToEdit = null
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
                        text = if (editing == null) "ثبت کالای جدید" else "ویرایش کالا",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )

                    if (error != null) {
                        Text(error!!, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = ValidationUtil.cleanSingleLine(it)
                            nameError = false
                            error = null
                        },
                        label = { Text("نام کالا یا خدمات (الزامی)") },
                        singleLine = true,
                        maxLines = 1,
                        isError = nameError,
                        supportingText = {
                            if (nameError) {
                                Text("این فیلد اجباری است — لطفاً نام کالا یا خدمات را وارد کنید", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = ValidationUtil.cleanSingleLine(it) },
                            label = { Text("کد کالا / بارکد") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = ValidationUtil.cleanSingleLine(it) },
                            label = { Text("دسته‌بندی") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = {
                            priceInput = ValidationUtil.cleanDigits(it)
                            priceError = false
                            error = null
                        },
                        label = { Text("قیمت فروش ($currency) (الزامی)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        maxLines = 1,
                        isError = priceError,
                        supportingText = {
                            if (priceError) {
                                Text("این فیلد اجباری است — قیمت فروش باید بزرگتر از صفر باشد", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick unit selection chips
                    Text("واحد سنجش:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(commonUnits) { u ->
                            FilterChip(
                                selected = unit == u,
                                onClick = { unit = u },
                                label = { Text(u, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("توضیحات و مشخصات فنی") },
                        minLines = 2,
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
                                productToEdit = null
                            }
                        ) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val prc = priceInput.toLongOrNull() ?: 0L
                                var hasErr = false
                                if (name.isBlank()) {
                                    nameError = true
                                    hasErr = true
                                }
                                if (prc <= 0) {
                                    priceError = true
                                    hasErr = true
                                }
                                if (hasErr) {
                                    error = "لطفاً فیلدهای اجباری مشخص‌شده را تکمیل نمایید."
                                    coroutineScope.launch { dialogScrollState.animateScrollTo(0) }
                                    return@Button
                                }

                                val toSave = Product(
                                    id = editing?.id ?: 0L,
                                    name = name,
                                    code = code,
                                    unit = unit,
                                    price = prc,
                                    category = category,
                                    description = description
                                )
                                viewModel.saveProduct(toSave) {
                                    showAddDialog = false
                                    productToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("ذخیره کالا")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val target = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("حذف کالا") },
            text = { Text("آیا از حذف «${target.name}» از انبار مطمئن هستید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(target)
                        productToDelete = null
                        Toast.makeText(context, "کالا با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Batch Price Adjustment Dialog
    if (showBatchPriceDialog && selectedProductIds.isNotEmpty()) {
        BatchPriceAdjustmentDialog(
            selectedCount = selectedProductIds.size,
            currency = currency,
            themeColor = themeColor,
            onDismissRequest = { showBatchPriceDialog = false },
            onConfirm = { isPercentage, amount, isIncrease ->
                viewModel.batchAdjustPrices(
                    productIds = selectedProductIds,
                    isPercentage = isPercentage,
                    amount = amount,
                    isIncrease = isIncrease,
                    onDone = {
                        showBatchPriceDialog = false
                        Toast.makeText(context, "قیمت ${selectedProductIds.size} کالا با موفقیت به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                        selectedProductIds = emptySet()
                    }
                )
            }
        )
    }

    // Batch Category Change Dialog
    if (showBatchCategoryDialog && selectedProductIds.isNotEmpty()) {
        BatchCategoryChangeDialog(
            selectedCount = selectedProductIds.size,
            existingCategories = categories,
            themeColor = themeColor,
            onDismissRequest = { showBatchCategoryDialog = false },
            onConfirm = { newCategory ->
                viewModel.batchChangeCategory(
                    productIds = selectedProductIds,
                    newCategory = newCategory,
                    onDone = {
                        showBatchCategoryDialog = false
                        Toast.makeText(context, "دسته‌بندی ${selectedProductIds.size} کالا با موفقیت تغییر یافت", Toast.LENGTH_SHORT).show()
                        selectedProductIds = emptySet()
                    }
                )
            }
        )
    }

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteConfirm && selectedProductIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("حذف گروهی کالاها") },
            text = { Text("آیا از حذف ${selectedProductIds.size} کالای انتخاب‌شده از انبار مطمئن هستید؟ این عملیات قابل بازگشت نیست.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batchDeleteProducts(
                            productIds = selectedProductIds,
                            onDone = {
                                val count = selectedProductIds.size
                                showBatchDeleteConfirm = false
                                selectedProductIds = emptySet()
                                Toast.makeText(context, "$count کالا با موفقیت حذف شدند", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف همه موارد انتخاب‌شده")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
