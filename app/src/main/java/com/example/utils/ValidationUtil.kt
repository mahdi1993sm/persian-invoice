package com.example.utils

object ValidationUtil {

    /**
     * Converts Persian and Arabic digits to standard English digits and strips newlines (\n, \r).
     */
    fun normalizeDigits(input: String): String {
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        val arabic = "٠١٢٣٤٥٦٧٨٩"
        val sb = StringBuilder()
        for (ch in input) {
            when {
                ch == '\n' || ch == '\r' -> continue
                ch in '۰'..'۹' -> sb.append(persian.indexOf(ch))
                ch in '٠'..'٩' -> sb.append(arabic.indexOf(ch))
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Strips newlines and removes all non-digit characters.
     */
    fun cleanDigits(input: String): String {
        return normalizeDigits(input).filter { it.isDigit() }
    }

    /**
     * Strips newlines (\n and \r) from text.
     */
    fun cleanSingleLine(input: String): String {
        return input.replace("\n", "").replace("\r", "")
    }

    /**
     * Cleans and sanitizes decimal numbers (e.g. discount and tax percentages).
     * Replaces Persian commas or slashes with dot, keeps max 1 dot.
     */
    fun cleanDecimal(input: String): String {
        val normalized = normalizeDigits(input)
            .replace("\n", "")
            .replace("\r", "")
            .replace("،", ".")
            .replace("/", ".")
        val sb = StringBuilder()
        var hasDot = false
        for (ch in normalized) {
            if (ch.isDigit()) {
                sb.append(ch)
            } else if (ch == '.' && !hasDot) {
                sb.append(ch)
                hasDot = true
            }
        }
        return sb.toString()
    }

    /**
     * Sanitizes mobile number: converts digits, handles international prefix if pasted,
     * strips newlines and limits to 11 digits.
     */
    fun cleanMobile(input: String): String {
        var digits = cleanDigits(input)
        if (digits.startsWith("00989") && digits.length >= 14) {
            digits = "0" + digits.substring(4)
        } else if (digits.startsWith("989") && digits.length >= 12) {
            digits = "0" + digits.substring(2)
        }
        return digits.take(11)
    }

    /**
     * Sanitizes National Code (کد ملی): limits to 10 numeric digits.
     */
    fun cleanNationalCode(input: String): String {
        return cleanDigits(input).take(10)
    }

    /**
     * Sanitizes Postal Code (کد پستی): limits to 10 numeric digits.
     */
    fun cleanPostalCode(input: String): String {
        return cleanDigits(input).take(10)
    }

    /**
     * Validates National Code (کد ملی): exactly 10 digits.
     */
    fun isValidNationalCode(code: String): Boolean {
        val cleaned = cleanDigits(code)
        return cleaned.length == 10
    }

    /**
     * Validates Mobile Number: must start with 09 and be exactly 11 digits.
     */
    fun isValidMobile(mobile: String): Boolean {
        val cleaned = cleanDigits(mobile)
        return cleaned.length == 11 && cleaned.startsWith("09")
    }

    /**
     * Validates Postal Code: exactly 10 digits.
     */
    fun isValidPostalCode(postalCode: String): Boolean {
        val cleaned = cleanDigits(postalCode)
        return cleaned.length == 10
    }
}
