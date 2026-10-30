package com.example.utils

import java.text.DecimalFormat
import java.util.Calendar

data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    val monthName: String
        get() = JalaliCalendar.MONTH_NAMES.getOrElse(month - 1) { "" }

    fun format(includeMonthName: Boolean = false): String {
        return if (includeMonthName) {
            "${day} ${monthName} ${year}"
        } else {
            String.format("%04d/%02d/%02d", year, month, day)
        }
    }
}

object JalaliCalendar {
    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    fun getToday(): JalaliDate {
        val calendar = Calendar.getInstance()
        return gregorianToJalali(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getTodayString(): String = getToday().format()

    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        gDayNo += gDaysInMonth[gm] + gd
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (jDayNo < 186) {
            jm = 1 + jDayNo / 31
            jd = 1 + (jDayNo % 31)
        } else {
            jm = 7 + (jDayNo - 186) / 30
            jd = 1 + ((jDayNo - 186) % 30)
        }

        return JalaliDate(jy, jm, jd)
    }

    fun parse(dateString: String): JalaliDate? {
        val parts = dateString.trim().split("/", "-")
        if (parts.size != 3) return null
        return try {
            val y = parts[0].trim().toInt()
            val m = parts[1].trim().toInt()
            val d = parts[2].trim().toInt()
            JalaliDate(y, m, d)
        } catch (_: Exception) {
            null
        }
    }

    fun daysInMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    fun isLeapYear(year: Int): Boolean {
        val breaks = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )
        var jp = breaks[0]
        var jump = 0
        for (j in 1 until breaks.size) {
            val jm = breaks[j]
            jump = jm - jp
            if (year < jm) break
            jp = jm
        }
        var n = year - jp
        if (jump - n < 6) n = n - jump + (jump + 4) / 33 * 33
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4
        return leap == 0
    }
}

object PersianNumberFormatter {
    private val decimalFormat = DecimalFormat("#,###")

    fun formatPrice(amount: Long, currency: String = "تومان"): String {
        val formatted = decimalFormat.format(amount)
        return "$formatted $currency"
    }

    fun formatNumber(number: Long): String {
        return decimalFormat.format(number)
    }

    fun toPersianDigits(text: String): String {
        val persianDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val builder = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                builder.append(persianDigits[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }
}
