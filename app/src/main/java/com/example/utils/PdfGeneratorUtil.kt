package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.data.db.InvoiceWithItems
import com.example.data.model.SellerInfo
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.min

data class PdfExportConfig(
    val includeSignatureAndStamp: Boolean = true,
    val paperSize: String = "A4", // "A4" or "A5"
    val isLandscape: Boolean = false // false = Portrait, true = Landscape
)

data class PagePlan(
    val pageNumber: Int,
    val startIndex: Int,
    val endIndex: Int, // exclusive
    val includesFooter: Boolean
)

object PdfGeneratorUtil {

    internal fun planPages(
        totalItems: Int,
        maxWithFooter: Int,
        maxWithoutFooter: Int
    ): List<PagePlan> {
        if (totalItems <= maxWithFooter) {
            return listOf(PagePlan(1, 0, totalItems, includesFooter = true))
        }

        val plans = mutableListOf<PagePlan>()
        var currentStart = 0
        var pageNum = 1

        while (currentStart < totalItems) {
            val remaining = totalItems - currentStart
            if (remaining <= maxWithFooter) {
                // All remaining items fit on this page WITH the footer
                plans.add(PagePlan(pageNum, currentStart, totalItems, includesFooter = true))
                break
            } else {
                val capacity = maxWithoutFooter.coerceAtLeast(1)
                // Fill this page as much as possible.
                // If remaining items fit without footer on this page, leave at least 1 item for the final page with footer.
                val itemsToTake = if (remaining <= capacity) {
                    (remaining - 1).coerceAtLeast(1)
                } else {
                    capacity
                }

                val nextEnd = currentStart + itemsToTake
                plans.add(PagePlan(pageNum, currentStart, nextEnd, includesFooter = false))
                currentStart = nextEnd
                pageNum++
            }
        }

        if (plans.isNotEmpty() && !plans.last().includesFooter) {
            plans.add(PagePlan(pageNum, totalItems, totalItems, includesFooter = true))
        }

        return plans
    }

    fun generateInvoicePdf(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        sellerInfo: SellerInfo?,
        currency: String = "تومان",
        themeColorHex: String = "#10B981",
        config: PdfExportConfig
    ): File {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val isA5 = config.paperSize.equals("A5", ignoreCase = true)
        val isLandscape = config.isLandscape

        // Compute page dimensions in points (72 points = 1 inch)
        val (pageWidth, pageHeight) = when {
            isA5 && isLandscape -> Pair(595, 420)
            isA5 -> Pair(420, 595)
            isLandscape -> Pair(842, 595)
            else -> Pair(595, 842)
        }

        val margin = when {
            isA5 && isLandscape -> 14f
            isA5 -> 18f
            isLandscape -> 20f
            else -> 24f
        }
        val contentWidth = pageWidth - (2 * margin)

        // Colors
        val themeColor = try {
            Color.parseColor(themeColorHex)
        } catch (_: Exception) {
            Color.rgb(16, 185, 129)
        }
        val darkGray = Color.rgb(30, 41, 59)
        val lightGray = Color.rgb(100, 116, 139)
        val borderGray = Color.rgb(203, 213, 225)
        val tableHeaderBg = Color.rgb(241, 245, 249)
        val tableAltRowBg = Color.rgb(248, 250, 252)

        // Proportional Heights & Spacing
        val accentBarHeight = if (isA5 && isLandscape) 4f else 5f
        val headerBoxHeight = when {
            isA5 && isLandscape -> 38f
            isA5 -> 44f
            isLandscape -> 46f
            else -> 50f
        }
        val partyBoxHeight = when {
            isA5 && isLandscape -> 38f
            isA5 -> 44f
            isLandscape -> 46f
            else -> 50f
        }
        val tableHeaderHeight = when {
            isA5 && isLandscape -> 18f
            isA5 -> 20f
            else -> 22f
        }
        val rowHeight = when {
            isA5 && isLandscape -> 16f
            isA5 -> 17f
            else -> 20f
        }
        val summaryHeight = when {
            isA5 && isLandscape -> 64f
            isA5 -> 74f
            else -> 82f
        }
        val sigBoxHeight = when {
            isA5 && isLandscape -> 38f
            isA5 -> 44f
            else -> 54f
        }
        val gap = if (isA5 && isLandscape) 5f else 7f

        val topHeaderTotalHeight = accentBarHeight + gap + headerBoxHeight + gap + partyBoxHeight + gap + tableHeaderHeight
        val footerTotalHeight = summaryHeight + (if (config.includeSignatureAndStamp) (gap + sigBoxHeight) else 0f) + gap

        val availableHeight = pageHeight - (2 * margin) - topHeaderTotalHeight
        val maxItemsWithFooter = ((availableHeight - footerTotalHeight) / rowHeight).toInt().coerceAtLeast(1)
        val maxItemsWithoutFooter = (availableHeight / rowHeight).toInt().coerceAtLeast(2)

        // Plan pagination
        val pagePlans = planPages(
            totalItems = items.size,
            maxWithFooter = maxItemsWithFooter,
            maxWithoutFooter = maxItemsWithoutFooter
        )
        val totalPages = pagePlans.size

        // Preload signature & stamp bitmaps once
        val customSigBitmap = if (config.includeSignatureAndStamp) {
            sellerInfo?.signatureImagePath?.let { ImageProcessingUtil.loadBitmapFromFile(it) }
        } else null
        val customStampBitmap = if (config.includeSignatureAndStamp) {
            sellerInfo?.stampImagePath?.let { ImageProcessingUtil.loadBitmapFromFile(it) }
        } else null

        val pdfDocument = PdfDocument()

        // Render each page
        for (plan in pagePlans) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, plan.pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paints
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = darkGray
                typeface = Typeface.DEFAULT
            }
            val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = darkGray
                typeface = Typeface.DEFAULT_BOLD
            }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderGray
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            canvas.drawColor(Color.WHITE)

            var currentY = margin

            // 1. Top Brand Banner Accent Bar
            paint.color = themeColor
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(margin, currentY, margin + contentWidth, currentY + accentBarHeight, 2.5f, 2.5f, paint)
            currentY += accentBarHeight + gap

            // 2. Invoice Document Header Box (Consistent and repeated on every page)
            borderPaint.strokeWidth = 1.2f
            canvas.drawRoundRect(margin, currentY, margin + contentWidth, currentY + headerBoxHeight, 8f, 8f, borderPaint)

            // Right side: Document Title and Store Name
            boldPaint.textSize = if (isA5) 12f else 14f
            boldPaint.color = darkGray
            boldPaint.textAlign = Paint.Align.RIGHT
            val titleText = if (totalPages > 1 && plan.pageNumber > 1) "صورتحساب فروش کالا و خدمات (ادامه)" else "صورتحساب فروش کالا و خدمات"
            canvas.drawText(titleText, margin + contentWidth - 12f, currentY + (if (isA5) 16f else 20f), boldPaint)

            textPaint.textSize = if (isA5) 8.5f else 9.5f
            textPaint.color = lightGray
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(sellerInfo?.name ?: "فروشگاه", margin + contentWidth - 12f, currentY + (if (isA5) 30f else 38f), textPaint)

            // Left side: Invoice Number, Date, and Page Number
            boldPaint.textAlign = Paint.Align.LEFT
            boldPaint.textSize = if (isA5) 8.5f else 10f
            canvas.drawText("شماره فاکتور: ${invoice.invoiceNumber}", margin + 12f, currentY + (if (isA5) 15f else 18f), boldPaint)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = if (isA5) 8f else 9f
            canvas.drawText("تاریخ صدور: ${invoice.createdAt}", margin + 12f, currentY + (if (isA5) 26f else 32f), textPaint)

            if (totalPages > 1) {
                boldPaint.textSize = if (isA5) 8f else 8.5f
                boldPaint.color = themeColor
                canvas.drawText("صفحه ${plan.pageNumber} از $totalPages", margin + 12f, currentY + (if (isA5) 36f else 46f), boldPaint)
                boldPaint.color = darkGray
            }

            currentY += headerBoxHeight + gap

            // 3. Seller and Customer Information Box (Consistent and repeated on every page)
            val halfWidth = (contentWidth - 10f) / 2f

            // Seller Box (Right)
            val sellerLeft = margin + halfWidth + 10f
            val sellerRight = margin + contentWidth
            borderPaint.strokeWidth = 0.8f
            canvas.drawRoundRect(sellerLeft, currentY, sellerRight, currentY + partyBoxHeight, 6f, 6f, borderPaint)

            boldPaint.textSize = if (isA5) 8f else 9.5f
            boldPaint.textAlign = Paint.Align.RIGHT
            boldPaint.color = themeColor
            canvas.drawText("مشخصات فروشنده", sellerRight - 8f, currentY + (if (isA5) 12f else 15f), boldPaint)

            textPaint.textSize = if (isA5) 7.5f else 8.5f
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = darkGray
            canvas.drawText("نام: ${sellerInfo?.name ?: "فروشگاه"}", sellerRight - 8f, currentY + (if (isA5) 23f else 29f), textPaint)
            val sellerContact = "تلفن: ${sellerInfo?.phone ?: "—"}  |  شناسه: ${sellerInfo?.nationalCode ?: "—"}"
            canvas.drawText(sellerContact, sellerRight - 8f, currentY + (if (isA5) 34f else 43f), textPaint)

            // Customer Box (Left)
            val custLeft = margin
            val custRight = margin + halfWidth
            canvas.drawRoundRect(custLeft, currentY, custRight, currentY + partyBoxHeight, 6f, 6f, borderPaint)

            boldPaint.textSize = if (isA5) 8f else 9.5f
            boldPaint.textAlign = Paint.Align.RIGHT
            boldPaint.color = themeColor
            canvas.drawText("مشخصات خریدار", custRight - 8f, currentY + (if (isA5) 12f else 15f), boldPaint)

            textPaint.textSize = if (isA5) 7.5f else 8.5f
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = darkGray
            canvas.drawText("خریدار: ${invoice.customerName}", custRight - 8f, currentY + (if (isA5) 23f else 29f), textPaint)
            val paymentDesc = when {
                invoice.paymentType == "credit" -> "تسویه: نسیه (سررسید: ${invoice.dueDate})"
                invoice.paymentType == "check" -> "تسویه: چک صیادی"
                invoice.paymentMethod.isNotBlank() -> "تسویه: ${invoice.paymentMethod}"
                else -> "تسویه: نقدی"
            }
            canvas.drawText(paymentDesc, custRight - 8f, currentY + (if (isA5) 34f else 43f), textPaint)

            currentY += partyBoxHeight + gap

            // 4. Items Table
            val colTotalW = contentWidth * 0.17f
            val colUnitPriceW = contentWidth * 0.16f
            val colUnitW = contentWidth * 0.08f
            val colQtyW = contentWidth * 0.08f
            val colIndexW = contentWidth * 0.07f
            val colDescW = contentWidth - (colTotalW + colUnitPriceW + colUnitW + colQtyW + colIndexW)

            val xIndex = margin + contentWidth - colIndexW
            val xDesc = xIndex - colDescW
            val xQty = xDesc - colQtyW
            val xUnit = xQty - colUnitW
            val xUnitPrice = xUnit - colUnitPriceW
            val xTotal = margin

            // Table Header Row
            paint.color = tableHeaderBg
            paint.style = Paint.Style.FILL
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + tableHeaderHeight, paint)
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + tableHeaderHeight, borderPaint)

            boldPaint.textSize = if (isA5) 7.5f else 8.5f
            boldPaint.textAlign = Paint.Align.CENTER
            boldPaint.color = darkGray
            val thTextY = currentY + (if (isA5) 13.5f else 15f)
            canvas.drawText("ردیف", xIndex + (colIndexW / 2f), thTextY, boldPaint)

            boldPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("شرح کالا یا خدمات", margin + contentWidth - colIndexW - 6f, thTextY, boldPaint)

            boldPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("تعداد", xQty + (colQtyW / 2f), thTextY, boldPaint)
            canvas.drawText("واحد", xUnit + (colUnitW / 2f), thTextY, boldPaint)

            boldPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("قیمت واحد ($currency)", xUnitPrice + colUnitPriceW - 6f, thTextY, boldPaint)
            canvas.drawText("مبلغ کل ($currency)", xTotal + colTotalW - 6f, thTextY, boldPaint)

            currentY += tableHeaderHeight

            // Table Item Rows for this page
            for (i in plan.startIndex until plan.endIndex) {
                val itm = items[i]
                if (i % 2 == 1) {
                    paint.color = tableAltRowBg
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(margin, currentY, margin + contentWidth, currentY + rowHeight, paint)
                }
                canvas.drawRect(margin, currentY, margin + contentWidth, currentY + rowHeight, borderPaint)

                textPaint.textSize = if (isA5) 7.5f else 8.5f
                val rowTextY = currentY + (if (isA5) 12.5f else 14f)

                // Index (continuous 1-based numbering across all pages)
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.color = darkGray
                canvas.drawText("${i + 1}", xIndex + (colIndexW / 2f), rowTextY, textPaint)

                // Description
                textPaint.textAlign = Paint.Align.RIGHT
                val maxChars = if (isA5) 28 else 38
                val descShort = if (itm.productName.length > maxChars) itm.productName.take(maxChars - 3) + "..." else itm.productName
                canvas.drawText(descShort, margin + contentWidth - colIndexW - 6f, rowTextY, textPaint)

                // Quantity
                textPaint.textAlign = Paint.Align.CENTER
                val qtyStr = if (itm.quantity % 1.0 == 0.0) "${itm.quantity.toInt()}" else "${itm.quantity}"
                canvas.drawText(qtyStr, xQty + (colQtyW / 2f), rowTextY, textPaint)

                // Unit
                canvas.drawText(itm.unit, xUnit + (colUnitW / 2f), rowTextY, textPaint)

                // Unit Price
                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(PersianNumberFormatter.formatNumber(itm.unitPrice), xUnitPrice + colUnitPriceW - 6f, rowTextY, textPaint)

                // Total Row Price
                boldPaint.textAlign = Paint.Align.RIGHT
                boldPaint.textSize = if (isA5) 7.5f else 8.5f
                boldPaint.color = darkGray
                canvas.drawText(PersianNumberFormatter.formatNumber(itm.totalRowPrice), xTotal + colTotalW - 6f, rowTextY, boldPaint)

                currentY += rowHeight
            }

            // 5. Draw Footer ONLY on the page marked with includesFooter
            if (plan.includesFooter) {
                currentY += 8f

                val summaryWidth = if (config.isLandscape) contentWidth * 0.45f else (if (isA5) 190f else 225f)
                val summaryLeft = margin
                val summaryRight = margin + summaryWidth
                val summaryTop = currentY

                // Summary Container
                canvas.drawRoundRect(summaryLeft, summaryTop, summaryRight, summaryTop + summaryHeight, 6f, 6f, borderPaint)

                var sY = summaryTop + (if (isA5) 14f else 16f)
                textPaint.textSize = if (isA5) 7.5f else 8.5f
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.color = lightGray
                canvas.drawText("جمع جزء اقلام:", summaryRight - 8f, sY, textPaint)
                boldPaint.textAlign = Paint.Align.LEFT
                boldPaint.textSize = if (isA5) 7.5f else 8.5f
                boldPaint.color = darkGray
                canvas.drawText(PersianNumberFormatter.formatPrice(invoice.subtotal, currency), summaryLeft + 8f, sY, boldPaint)

                if (invoice.discountAmount > 0) {
                    sY += if (isA5) 12f else 14f
                    textPaint.textAlign = Paint.Align.RIGHT
                    textPaint.color = Color.rgb(16, 185, 129)
                    canvas.drawText("تخفیف (${invoice.discountPercent}%):", summaryRight - 8f, sY, textPaint)
                    boldPaint.textAlign = Paint.Align.LEFT
                    boldPaint.color = Color.rgb(16, 185, 129)
                    canvas.drawText("- " + PersianNumberFormatter.formatPrice(invoice.discountAmount, currency), summaryLeft + 8f, sY, boldPaint)
                }

                if (invoice.taxAmount > 0) {
                    sY += if (isA5) 12f else 14f
                    textPaint.textAlign = Paint.Align.RIGHT
                    textPaint.color = lightGray
                    canvas.drawText("مالیات (${invoice.taxPercent}%):", summaryRight - 8f, sY, textPaint)
                    boldPaint.textAlign = Paint.Align.LEFT
                    boldPaint.color = darkGray
                    canvas.drawText("+ " + PersianNumberFormatter.formatPrice(invoice.taxAmount, currency), summaryLeft + 8f, sY, boldPaint)
                }

                sY += if (isA5) 14f else 16f
                paint.color = themeColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.8f
                canvas.drawLine(summaryLeft + 8f, sY - 5f, summaryRight - 8f, sY - 5f, paint)

                boldPaint.textAlign = Paint.Align.RIGHT
                boldPaint.textSize = if (isA5) 8.5f else 10f
                boldPaint.color = themeColor
                canvas.drawText("مبلغ کل نهایی:", summaryRight - 8f, sY + 6f, boldPaint)

                boldPaint.textAlign = Paint.Align.LEFT
                boldPaint.textSize = if (isA5) 8.5f else 10f
                boldPaint.color = themeColor
                canvas.drawText(PersianNumberFormatter.formatPrice(invoice.totalAmount, currency), summaryLeft + 8f, sY + 6f, boldPaint)

                // Notes and Payment Terms Card (Right of summary)
                val notesLeft = summaryRight + 10f
                val notesRight = margin + contentWidth
                if (notesRight > notesLeft) {
                    canvas.drawRoundRect(notesLeft, summaryTop, notesRight, summaryTop + summaryHeight, 6f, 6f, borderPaint)

                    boldPaint.textSize = if (isA5 && isLandscape) 7.5f else if (isA5) 8f else 9f
                    boldPaint.textAlign = Paint.Align.RIGHT
                    boldPaint.color = darkGray
                    canvas.drawText("اطلاعات پرداخت و شرایط مالی:", notesRight - 8f, summaryTop + (if (isA5 && isLandscape) 12f else 14f), boldPaint)

                    textPaint.textSize = if (isA5 && isLandscape) 6.8f else if (isA5) 7.2f else 8f
                    textPaint.textAlign = Paint.Align.RIGHT
                    textPaint.color = darkGray
                    var nY = summaryTop + (if (isA5 && isLandscape) 22f else 26f)
                    val lineStep = if (isA5 && isLandscape) 9.5f else if (isA5) 11.5f else 13f

                    // 1. Payment amount & date
                    val payDateStr = if (invoice.paymentDate.isNotBlank()) invoice.paymentDate else invoice.createdAt
                    val payAmountStr = if (invoice.paidAmount > 0) PersianNumberFormatter.formatPrice(invoice.paidAmount, currency) else "—"
                    val payLine1 = "مبلغ پرداختی: $payAmountStr  |  تاریخ: $payDateStr"
                    canvas.drawText(payLine1, notesRight - 8f, nY, textPaint)
                    nY += lineStep

                    // 2. Payment method & destination account
                    val methodStr = invoice.paymentMethod.ifBlank {
                        when (invoice.paymentType) {
                            "cash" -> when (invoice.cashPaymentMethod) {
                                "pos" -> "کارتخوان (پوز)"
                                "card_to_card" -> "کارت به کارت"
                                "paya" -> "پایا / ساتنا"
                                else -> "نقدی"
                            }
                            "credit" -> "نسیه"
                            "check" -> "چک صیادی"
                            else -> invoice.paymentType
                        }
                    }
                    val accStr = if (invoice.paymentAccountInfo.isNotBlank()) {
                        invoice.paymentAccountInfo
                    } else if (invoice.bankAccountName.isNotBlank()) {
                        invoice.bankAccountName
                    } else {
                        "—"
                    }
                    val payLine2 = "روش: $methodStr  |  به حساب: $accStr"
                    val maxLine2Chars = if (isA5 && isLandscape) 48 else if (isA5) 36 else 52
                    val truncatedLine2 = if (payLine2.length > maxLine2Chars) payLine2.take(maxLine2Chars - 3) + "..." else payLine2
                    canvas.drawText(truncatedLine2, notesRight - 8f, nY, textPaint)
                    nY += lineStep

                    // 3. Tracking code, credit remaining, or check info
                    if (invoice.remainingAmount > 0) {
                        textPaint.color = Color.rgb(220, 38, 38)
                        val dueStr = if (invoice.dueDate.isNotBlank()) " (سررسید: ${invoice.dueDate})" else ""
                        val creditLine = "مانده بدهی: ${PersianNumberFormatter.formatPrice(invoice.remainingAmount, currency)}$dueStr"
                        canvas.drawText(creditLine, notesRight - 8f, nY, textPaint)
                        textPaint.color = darkGray
                        nY += lineStep
                    } else if (invoice.paymentTrackingCode.isNotBlank()) {
                        canvas.drawText("کد پیگیری تراکنش: ${invoice.paymentTrackingCode}", notesRight - 8f, nY, textPaint)
                        nY += lineStep
                    } else if (invoice.paymentType == "check" && invoice.checkSayadiId.isNotBlank()) {
                        canvas.drawText("چک صیاد: ${invoice.checkSayadiId} | بانک: ${invoice.checkBankName}", notesRight - 8f, nY, textPaint)
                        nY += lineStep
                    }

                    // 4. Terms / description
                    val footer = invoice.description.ifBlank { sellerInfo?.footerText ?: "کالاهای تحویل شده با سلامت فیزیکی کامل تحویل داده شد." }
                    val maxFooterChars = if (isA5 && isLandscape) 52 else if (isA5) 38 else 55
                    val truncatedFooter = if (footer.length > maxFooterChars) footer.take(maxFooterChars - 3) + "..." else footer
                    textPaint.color = lightGray
                    canvas.drawText(truncatedFooter, notesRight - 8f, nY, textPaint)
                }

                currentY += summaryHeight + gap

                // 6. Signatures and Official Stamp (If Enabled)
                if (config.includeSignatureAndStamp) {
                    val sigHalfW = (contentWidth - 12f) / 2f

                    // Customer Signature Box (Right)
                    val sigCustLeft = margin + sigHalfW + 12f
                    val sigCustRight = margin + contentWidth
                    canvas.drawRoundRect(sigCustLeft, currentY, sigCustRight, currentY + sigBoxHeight, 6f, 6f, borderPaint)

                    textPaint.textSize = if (isA5) 7.5f else 8.5f
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.color = lightGray
                    canvas.drawText("مهر و امضای خریدار", sigCustLeft + (sigHalfW / 2f), currentY + 14f, textPaint)

                    // Seller Stamp & Signature Box (Left)
                    val sigSellerLeft = margin
                    val sigSellerRight = margin + sigHalfW
                    canvas.drawRoundRect(sigSellerLeft, currentY, sigSellerRight, currentY + sigBoxHeight, 6f, 6f, borderPaint)

                    canvas.drawText("مهر و امضای فروشنده", sigSellerLeft + (sigHalfW / 2f), currentY + 13f, textPaint)
                    boldPaint.textSize = if (isA5) 7.5f else 8f
                    boldPaint.textAlign = Paint.Align.CENTER
                    boldPaint.color = themeColor
                    canvas.drawText(sellerInfo?.signatureName ?: "مدیریت فروشگاه", sigSellerLeft + (sigHalfW / 2f), currentY + 24f, boldPaint)

                    val maxImageH = sigBoxHeight - 24f
                    val maxImageW = (sigHalfW / 2f) - 6f

                    if (customSigBitmap != null) {
                        val scale = min(maxImageW / customSigBitmap.width, maxImageH / customSigBitmap.height)
                        val destW = (customSigBitmap.width * scale).toInt().coerceAtLeast(1)
                        val destH = (customSigBitmap.height * scale).toInt().coerceAtLeast(1)
                        val destRect = RectF(
                            sigSellerLeft + 8f,
                            currentY + 20f,
                            sigSellerLeft + 8f + destW,
                            currentY + 20f + destH
                        )
                        canvas.drawBitmap(customSigBitmap, null, destRect, null)
                    }

                    if (customStampBitmap != null) {
                        val scale = min(maxImageW / customStampBitmap.width, maxImageH / customStampBitmap.height)
                        val destW = (customStampBitmap.width * scale).toInt().coerceAtLeast(1)
                        val destH = (customStampBitmap.height * scale).toInt().coerceAtLeast(1)
                        val destRect = RectF(
                            sigSellerRight - destW - 8f,
                            currentY + 18f,
                            sigSellerRight - 8f,
                            currentY + 18f + destH
                        )
                        canvas.drawBitmap(customStampBitmap, null, destRect, null)
                    } else if (customSigBitmap == null) {
                        // Fallback circular decorative stamp outline
                        val stampCenterX = sigSellerLeft + (sigHalfW / 2f)
                        val stampCenterY = currentY + (sigBoxHeight / 2f) + 4f
                        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = themeColor
                            style = Paint.Style.STROKE
                            strokeWidth = 0.8f
                            alpha = 80
                        }
                        canvas.drawCircle(stampCenterX, stampCenterY, 15f, stampPaint)
                        canvas.drawCircle(stampCenterX, stampCenterY, 12f, stampPaint)
                    }
                }
            }

            // Subtle page footer at the very bottom edge of every page
            val footerTextY = pageHeight - margin + 10f
            if (footerTextY <= pageHeight - 3f) {
                val pageFooterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(148, 163, 184)
                    textSize = if (isA5) 6.5f else 7.5f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "صفحه ${plan.pageNumber} از $totalPages  |  ${sellerInfo?.name ?: "سیستم صدور فاکتور فروش"}",
                    pageWidth / 2f,
                    footerTextY,
                    pageFooterPaint
                )
            }

            pdfDocument.finishPage(page)
        }

        // Save to file in cache
        val outputDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val cleanNumber = invoice.invoiceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val pdfFile = File(outputDir, "Invoice_${cleanNumber}.pdf")

        FileOutputStream(pdfFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Downloads the PDF directly to the device's public Downloads directory.
     * Compatible with Android 10+ (API 29+) Scoped Storage via MediaStore.Downloads.
     */
    fun downloadPdfToDevice(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        sellerInfo: SellerInfo?,
        currency: String = "تومان",
        themeColorHex: String = "#10B981",
        config: PdfExportConfig
    ): File {
        // 1. Generate base PDF file
        val tempFile = generateInvoicePdf(
            context,
            invoiceWithItems,
            sellerInfo,
            currency,
            themeColorHex,
            config
        )

        val invoiceNumber = invoiceWithItems.invoice.invoiceNumber
        val cleanNumber = invoiceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "Invoice_${cleanNumber}.pdf"

        // 2. Save directly into device Downloads
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val contentUri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                if (contentUri != null) {
                    context.contentResolver.openOutputStream(contentUri)?.use { outputStream ->
                        tempFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                tempFile.copyTo(targetFile, overwrite = true)
            }
        } catch (_: Exception) {
            // Fallback: file is already safely in app external storage/cache
        }

        // Post download notification so user can track status and tap to open
        showDownloadNotification(context, tempFile, invoiceNumber)

        return tempFile
    }

    private fun showDownloadNotification(context: Context, file: File, invoiceNumber: String) {
        try {
            val channelId = "pdf_download_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "دانلود فاکتورها",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "نمایش وضعیت دانلود فایل‌های PDF فاکتورها"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                invoiceNumber.hashCode(),
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("دانلود فاکتور $invoiceNumber")
                .setContentText("فایل با موفقیت در پوشه Downloads ذخیره شد (برای باز کردن لمس کنید)")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notificationManager.notify(invoiceNumber.hashCode(), notification)
        } catch (_: Exception) {
            // Gracefully ignore notification errors if permissions are disabled on device
        }
    }

    /**
     * Shares the generated PDF file via the Android system share sheet.
     */
    fun sharePdfFile(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        sellerInfo: SellerInfo?,
        currency: String = "تومان",
        themeColorHex: String = "#10B981",
        config: PdfExportConfig
    ) {
        val pdfFile = generateInvoicePdf(
            context,
            invoiceWithItems,
            sellerInfo,
            currency,
            themeColorHex,
            config
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "فاکتور ${invoiceWithItems.invoice.invoiceNumber}")
            putExtra(Intent.EXTRA_TEXT, "فایل PDF فاکتور شماره ${invoiceWithItems.invoice.invoiceNumber} مربوط به «${invoiceWithItems.invoice.customerName}» ارسال گردید.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری PDF فاکتور"))
    }
}
