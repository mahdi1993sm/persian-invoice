package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.model.SellerInfo
import com.example.ui.InvoiceViewModel
import com.example.ui.components.BackgroundRemovalDialog
import com.example.utils.ImageProcessingUtil
import com.example.utils.ValidationUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerSettingsScreen(viewModel: InvoiceViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val currentSeller by viewModel.sellerInfo.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    var name by remember { mutableStateOf(currentSeller?.name ?: "") }
    var phone by remember { mutableStateOf(currentSeller?.phone ?: "") }
    var mobile by remember { mutableStateOf(currentSeller?.mobile ?: "") }
    var address by remember { mutableStateOf(currentSeller?.address ?: "") }
    var postalCode by remember { mutableStateOf(currentSeller?.postalCode ?: "") }
    var province by remember { mutableStateOf(currentSeller?.province ?: "") }
    var city by remember { mutableStateOf(currentSeller?.city ?: "") }
    var nationalCode by remember { mutableStateOf(currentSeller?.nationalCode ?: "") }
    var economicCode by remember { mutableStateOf(currentSeller?.economicCode ?: "") }
    var signatureName by remember { mutableStateOf(currentSeller?.signatureName ?: "") }
    var signatureImagePath by remember { mutableStateOf(currentSeller?.signatureImagePath ?: "") }
    var stampImagePath by remember { mutableStateOf(currentSeller?.stampImagePath ?: "") }
    var footerText by remember { mutableStateOf(currentSeller?.footerText ?: "") }
    var isSaved by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog state for background removal
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingTarget by remember { mutableStateOf<String?>(null) } // "signature" or "stamp"

    // Bitmaps for live UI preview
    var signatureBitmap by remember(signatureImagePath) {
        mutableStateOf(ImageProcessingUtil.loadBitmapFromFile(signatureImagePath))
    }
    var stampBitmap by remember(stampImagePath) {
        mutableStateOf(ImageProcessingUtil.loadBitmapFromFile(stampImagePath))
    }

    // Launchers for Signature
    val signatureGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = ImageProcessingUtil.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                pendingBitmap = bitmap
                pendingTarget = "signature"
            }
        }
    }

    val signatureCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            pendingBitmap = bitmap
            pendingTarget = "signature"
        }
    }

    // Launchers for Stamp
    val stampGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = ImageProcessingUtil.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                pendingBitmap = bitmap
                pendingTarget = "stamp"
            }
        }
    }

    val stampCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            pendingBitmap = bitmap
            pendingTarget = "stamp"
        }
    }

    LaunchedEffect(currentSeller) {
        if (currentSeller != null) {
            name = currentSeller!!.name
            phone = currentSeller!!.phone
            mobile = currentSeller!!.mobile
            address = currentSeller!!.address
            postalCode = currentSeller!!.postalCode
            province = currentSeller!!.province
            city = currentSeller!!.city
            nationalCode = currentSeller!!.nationalCode
            economicCode = currentSeller!!.economicCode
            signatureName = currentSeller!!.signatureName
            signatureImagePath = currentSeller!!.signatureImagePath
            stampImagePath = currentSeller!!.stampImagePath
            footerText = currentSeller!!.footerText
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مشخصات فروشگاه و مهر/امضا") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("seller_settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                Toast.makeText(context, "نام فروشگاه الزامی است و نمی‌تواند خالی باشد", Toast.LENGTH_LONG).show()
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(600)
                                }
                                return@Button
                            }
                            if (mobile.isNotBlank() && !ValidationUtil.isValidMobile(mobile)) {
                                Toast.makeText(context, "شماره موبایل باید ۱۱ رقم بوده و با ۰۹ آغاز شود", Toast.LENGTH_LONG).show()
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(600)
                                }
                                return@Button
                            }
                            if (nationalCode.isNotBlank() && !ValidationUtil.isValidNationalCode(nationalCode)) {
                                Toast.makeText(context, "کد ملی / شناسه ملی باید ۱۰ رقم باشد", Toast.LENGTH_LONG).show()
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(600)
                                }
                                return@Button
                            }
                            if (postalCode.isNotBlank() && !ValidationUtil.isValidPostalCode(postalCode)) {
                                Toast.makeText(context, "کد پستی باید ۱۰ رقم باشد", Toast.LENGTH_LONG).show()
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(600)
                                }
                                return@Button
                            }
                            val updated = SellerInfo(
                                id = 1,
                                name = name.trim(),
                                phone = phone.trim(),
                                mobile = mobile.trim(),
                                address = address.trim(),
                                postalCode = postalCode.trim(),
                                province = province.trim(),
                                city = city.trim(),
                                nationalCode = nationalCode.trim(),
                                economicCode = economicCode.trim(),
                                signatureName = signatureName.trim(),
                                signatureImagePath = signatureImagePath,
                                stampImagePath = stampImagePath,
                                footerText = footerText.trim()
                            )
                            viewModel.saveSellerInfo(updated) {
                                isSaved = true
                                Toast.makeText(context, "اطلاعات فروشگاه با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_seller_info_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ذخیره")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .testTag("seller_settings_scrollable"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isSaved) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                        Text("اطلاعات فروشگاه با موفقیت ذخیره گردید.", color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Overview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = themeColor, modifier = Modifier.size(36.dp))
                    Column {
                        Text(
                            text = name.ifBlank { "نام فروشگاه شما" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "این مشخصات، مهر و امضا در بالای فاکتورها و فرم خروجی PDF درج می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section: Store Signature (امضای فروشگاه)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Draw, contentDescription = null, tint = themeColor)
                        Text(
                            text = "امضای رسمی فروشگاه",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "تصویر امضا را از کاغذ عکاسی یا بارگذاری نمایید؛ برنامه زمینه کاغذی آن را به صورت خودکار حذف می‌کند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Preview Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        // Checkerboard pattern
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val squareSize = 14f
                            val cols = (size.width / squareSize).toInt() + 1
                            val rows = (size.height / squareSize).toInt() + 1
                            for (r in 0 until rows) {
                                for (c in 0 until cols) {
                                    val color = if ((r + c) % 2 == 0) Color(0xFFF8FAFC) else Color(0xFFE2E8F0)
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(c * squareSize, r * squareSize),
                                        size = Size(squareSize, squareSize)
                                    )
                                }
                            }
                        }

                        if (signatureBitmap != null) {
                            Image(
                                bitmap = signatureBitmap!!.asImageBitmap(),
                                contentDescription = "پیش‌نمایش امضای شفاف",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "هنوز تصویری برای امضا ثبت نشده است",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action Buttons for Signature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { signatureGalleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_signature_gallery")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("انتخاب از گالری", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { signatureCameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("camera_signature")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عکاسی امضا", style = MaterialTheme.typography.labelSmall)
                        }

                        if (signatureBitmap != null) {
                            IconButton(
                                onClick = {
                                    signatureImagePath = ""
                                    signatureBitmap = null
                                    isSaved = false
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف امضا", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }

            // Section: Store Stamp (مهر رسمی فروشگاه)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = themeColor)
                        Text(
                            text = "مهر رسمی فروشگاه / شرکت",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "تصویر مهر را بارگذاری یا عکاسی کنید تا پس‌زمینه آن حذف شده و در فاکتورها ثبت گردد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Stamp Preview Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val squareSize = 14f
                            val cols = (size.width / squareSize).toInt() + 1
                            val rows = (size.height / squareSize).toInt() + 1
                            for (r in 0 until rows) {
                                for (c in 0 until cols) {
                                    val color = if ((r + c) % 2 == 0) Color(0xFFF8FAFC) else Color(0xFFE2E8F0)
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(c * squareSize, r * squareSize),
                                        size = Size(squareSize, squareSize)
                                    )
                                }
                            }
                        }

                        if (stampBitmap != null) {
                            Image(
                                bitmap = stampBitmap!!.asImageBitmap(),
                                contentDescription = "پیش‌نمایش مهر شفاف",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "هنوز تصویری برای مهر ثبت نشده است",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action Buttons for Stamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { stampGalleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_stamp_gallery")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("انتخاب از گالری", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { stampCameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("camera_stamp")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عکاسی مهر", style = MaterialTheme.typography.labelSmall)
                        }

                        if (stampBitmap != null) {
                            IconButton(
                                onClick = {
                                    stampImagePath = ""
                                    stampBitmap = null
                                    isSaved = false
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف مهر", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }

            // Text Inputs
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = ValidationUtil.cleanSingleLine(it)
                    nameError = false
                    isSaved = false
                },
                label = { Text("نام فروشگاه یا شرکت (الزامی)") },
                singleLine = true,
                maxLines = 1,
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("این فیلد اجباری است — لطفاً نام فروشگاه یا شرکت را وارد کنید", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            val isMobileValid = mobile.isBlank() || ValidationUtil.isValidMobile(mobile)
            val isNationalValid = nationalCode.isBlank() || ValidationUtil.isValidNationalCode(nationalCode)
            val isPostalValid = postalCode.isBlank() || ValidationUtil.isValidPostalCode(postalCode)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = ValidationUtil.cleanDigits(it).take(11); isSaved = false },
                    label = { Text("تلفن ثابت") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = ValidationUtil.cleanMobile(it); isSaved = false },
                    label = { Text("تلفن همراه (۱۱ رقم با ۰۹)") },
                    placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    maxLines = 1,
                    isError = !isMobileValid,
                    supportingText = {
                        if (!isMobileValid) {
                            Text("باید ۱۱ رقم و با ۰۹ شروع شود (${mobile.length}/۱۱)", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                        } else if (mobile.isNotBlank()) {
                            Text("${mobile.length}/۱۱ رقم", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = province,
                    onValueChange = { province = ValidationUtil.cleanSingleLine(it); isSaved = false },
                    label = { Text("استان") },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = ValidationUtil.cleanSingleLine(it); isSaved = false },
                    label = { Text("شهر") },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it; isSaved = false },
                label = { Text("آدرس کامل") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nationalCode,
                    onValueChange = { nationalCode = ValidationUtil.cleanNationalCode(it); isSaved = false },
                    label = { Text("کد ملی / شناسه ملی (۱۰ رقم)") },
                    placeholder = { Text("۱۰ رقم عددی") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    maxLines = 1,
                    isError = !isNationalValid,
                    supportingText = {
                        if (!isNationalValid) {
                            Text("کد ملی باید ۱۰ رقم باشد (${nationalCode.length}/۱۰)", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                        } else if (nationalCode.isNotBlank()) {
                            Text("${nationalCode.length}/۱۰ رقم", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = economicCode,
                    onValueChange = { economicCode = ValidationUtil.cleanDigits(it).take(14); isSaved = false },
                    label = { Text("کد اقتصادی") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = postalCode,
                onValueChange = { postalCode = ValidationUtil.cleanPostalCode(it); isSaved = false },
                label = { Text("کد پستی ۱۰ رقمی") },
                placeholder = { Text("۱۰ رقم عددی") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                maxLines = 1,
                isError = !isPostalValid,
                supportingText = {
                    if (!isPostalValid) {
                        Text("کد پستی باید ۱۰ رقم باشد (${postalCode.length}/۱۰)", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                    } else if (postalCode.isNotBlank()) {
                        Text("${postalCode.length}/۱۰ رقم", style = MaterialTheme.typography.labelSmall)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = signatureName,
                onValueChange = { signatureName = ValidationUtil.cleanSingleLine(it); isSaved = false },
                label = { Text("عنوان امضاکننده فاکتور (مثلاً: مدیریت فروش)") },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = footerText,
                onValueChange = { footerText = it; isSaved = false },
                label = { Text("متن پاورقی و شرایط عمومی فاکتور") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Background Removal Dialog
    if (pendingBitmap != null && pendingTarget != null) {
        val target = pendingTarget!!
        val title = if (target == "signature") "امضا" else "مهر"

        BackgroundRemovalDialog(
            originalBitmap = pendingBitmap!!,
            title = title,
            themeColor = themeColor,
            onDismissRequest = {
                pendingBitmap = null
                pendingTarget = null
            },
            onConfirm = { processed ->
                val fileName = "seller_${target}_${System.currentTimeMillis()}"
                val savedPath = ImageProcessingUtil.saveBitmapAsPng(context, processed, fileName)
                if (target == "signature") {
                    signatureImagePath = savedPath
                    signatureBitmap = processed
                } else {
                    stampImagePath = savedPath
                    stampBitmap = processed
                }
                isSaved = false
                pendingBitmap = null
                pendingTarget = null
                Toast.makeText(context, "پس‌زمینه $title با موفقیت حذف و ذخیره شد.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
