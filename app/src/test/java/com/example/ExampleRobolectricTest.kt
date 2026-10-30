package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("فاکتور فروش", appName)
  }

  @Test
  fun `verify jalali calendar conversion`() {
    val jalali = JalaliCalendar.gregorianToJalali(2026, 10, 28)
    assertNotNull(jalali)
    assertEquals(1405, jalali.year)
    assertEquals(8, jalali.month)
  }

  @Test
  fun `verify multi-page pagination planner`() {
    // Single page scenario: 10 items with maxWithFooter = 20, maxWithoutFooter = 30
    val singlePagePlan = com.example.utils.PdfGeneratorUtil.planPages(
        totalItems = 10,
        maxWithFooter = 20,
        maxWithoutFooter = 30
    )
    assertEquals(1, singlePagePlan.size)
    assertEquals(0, singlePagePlan[0].startIndex)
    assertEquals(10, singlePagePlan[0].endIndex)
    org.junit.Assert.assertTrue(singlePagePlan[0].includesFooter)

    // Multi page scenario: 35 items with maxWithFooter = 20, maxWithoutFooter = 30
    val multiPagePlan = com.example.utils.PdfGeneratorUtil.planPages(
        totalItems = 35,
        maxWithFooter = 20,
        maxWithoutFooter = 30
    )
    assertEquals(2, multiPagePlan.size)
    // Page 1
    assertEquals(1, multiPagePlan[0].pageNumber)
    assertEquals(0, multiPagePlan[0].startIndex)
    assertEquals(30, multiPagePlan[0].endIndex)
    org.junit.Assert.assertFalse(multiPagePlan[0].includesFooter)
    // Page 2
    assertEquals(2, multiPagePlan[1].pageNumber)
    assertEquals(30, multiPagePlan[1].startIndex)
    assertEquals(35, multiPagePlan[1].endIndex)
    org.junit.Assert.assertTrue(multiPagePlan[1].includesFooter)

    // Large multi-page scenario: 70 items with maxWithFooter = 20, maxWithoutFooter = 30
    val largePlan = com.example.utils.PdfGeneratorUtil.planPages(
        totalItems = 70,
        maxWithFooter = 20,
        maxWithoutFooter = 30
    )
    assertEquals(3, largePlan.size)
    assertEquals(0, largePlan[0].startIndex)
    assertEquals(30, largePlan[0].endIndex)
    assertEquals(30, largePlan[1].startIndex)
    assertEquals(60, largePlan[1].endIndex)
    assertEquals(60, largePlan[2].startIndex)
    assertEquals(70, largePlan[2].endIndex)
    org.junit.Assert.assertTrue(largePlan[2].includesFooter)
  }

  @Test
  fun `verify boundary pagination splits properly without overflowing`() {
    // Exactly boundary case: 24 items where maxWithFooter is 23
    val boundaryPlan = com.example.utils.PdfGeneratorUtil.planPages(
        totalItems = 24,
        maxWithFooter = 23,
        maxWithoutFooter = 31
    )
    // Must be split across 2 pages so footer doesn't overflow!
    assertEquals(2, boundaryPlan.size)
    assertEquals(1, boundaryPlan[0].pageNumber)
    org.junit.Assert.assertFalse(boundaryPlan[0].includesFooter)
    // Page 1 fills up leaving 1 item for the final page
    assertEquals(23, boundaryPlan[0].endIndex)
    assertEquals(2, boundaryPlan[1].pageNumber)
    org.junit.Assert.assertTrue(boundaryPlan[1].includesFooter)
    assertEquals(24, boundaryPlan[1].endIndex)
  }

  @Test
  fun `verify page 1 is filled first before going to page 2`() {
    // Scenario: 8 items where maxWithFooter = 5, maxWithoutFooter = 13
    // Page 1 must not be half-empty! It must take (8 - 1) = 7 items, Page 2 takes 1 item + footer
    val plan = com.example.utils.PdfGeneratorUtil.planPages(
        totalItems = 8,
        maxWithFooter = 5,
        maxWithoutFooter = 13
    )
    assertEquals(2, plan.size)
    // Page 1 takes 7 items (leaving only 1 for page 2)
    assertEquals(0, plan[0].startIndex)
    assertEquals(7, plan[0].endIndex)
    org.junit.Assert.assertFalse(plan[0].includesFooter)

    // Page 2 takes the remaining 1 item and includes footer
    assertEquals(7, plan[1].startIndex)
    assertEquals(8, plan[1].endIndex)
    org.junit.Assert.assertTrue(plan[1].includesFooter)

    // And if 8 items fit within maxWithFooter = 10:
    val fitSinglePage = com.example.utils.PdfGeneratorUtil.planPages(
        totalItems = 8,
        maxWithFooter = 10,
        maxWithoutFooter = 16
    )
    assertEquals(1, fitSinglePage.size)
    assertEquals(8, fitSinglePage[0].endIndex)
    org.junit.Assert.assertTrue(fitSinglePage[0].includesFooter)
  }

  @Test
  fun `verify financial conditions in Invoice entity`() {
    val invoice = com.example.data.model.Invoice(
        invoiceNumber = "INV-1403-100",
        customerId = 1L,
        customerName = "مشتری نمونه",
        createdAt = "1403/08/15",
        paymentDate = "1403/08/16",
        paymentMethod = "کارتخوان (پوز)",
        paymentAccountInfo = "بانک ملت - حساب سپهر (۶۰۳۷)",
        paymentTrackingCode = "84920194",
        paidAmount = 75000000L,
        totalAmount = 75000000L
    )
    assertEquals("1403/08/16", invoice.paymentDate)
    assertEquals("کارتخوان (پوز)", invoice.paymentMethod)
    assertEquals("بانک ملت - حساب سپهر (۶۰۳۷)", invoice.paymentAccountInfo)
    assertEquals("84920194", invoice.paymentTrackingCode)
    assertEquals(75000000L, invoice.paidAmount)
  }

  @Test
  fun `verify validation and newline stripping for numeric and identity fields`() {
    val validationUtil = com.example.utils.ValidationUtil

    // 1. Newline stripping
    val withNewlines = "123\n45\r67"
    assertEquals("1234567", validationUtil.cleanDigits(withNewlines))
    assertEquals("1234567", validationUtil.cleanSingleLine(withNewlines))

    // Decimal discount/tax newline stripping
    val decimalWithEnter = "9\n.\r5"
    assertEquals("9.5", validationUtil.cleanDecimal(decimalWithEnter))

    // 2. National Code validation (exactly 10 digits)
    org.junit.Assert.assertTrue(validationUtil.isValidNationalCode("0012345678"))
    org.junit.Assert.assertTrue(validationUtil.isValidNationalCode("۰۰۱۲۳۴۵۶۷۸")) // Persian digits
    org.junit.Assert.assertFalse(validationUtil.isValidNationalCode("123456789")) // 9 digits
    org.junit.Assert.assertFalse(validationUtil.isValidNationalCode("12345678901")) // 11 digits
    org.junit.Assert.assertFalse(validationUtil.isValidNationalCode("abc1234567")) // letters
    assertEquals("0012345678", validationUtil.cleanNationalCode("0012345678999")) // takes first 10

    // 3. Mobile Number validation (11 digits, starts with 09)
    org.junit.Assert.assertTrue(validationUtil.isValidMobile("09123456789"))
    org.junit.Assert.assertTrue(validationUtil.isValidMobile("۰۹۱۲۳۴۵۶۷۸۹")) // Persian digits
    org.junit.Assert.assertFalse(validationUtil.isValidMobile("08123456789")) // Doesn't start with 09
    org.junit.Assert.assertFalse(validationUtil.isValidMobile("0912345678")) // 10 digits
    org.junit.Assert.assertFalse(validationUtil.isValidMobile("091234567890")) // 12 digits
    assertEquals("09123456789", validationUtil.cleanMobile("+989123456789")) // Normalizes +98
    assertEquals("09123456789", validationUtil.cleanMobile("09123456789000")) // Limits to 11

    // 4. Postal Code validation (exactly 10 digits)
    org.junit.Assert.assertTrue(validationUtil.isValidPostalCode("1987654321"))
    org.junit.Assert.assertTrue(validationUtil.isValidPostalCode("۱۹۸۷۶۵۴۳۲۱")) // Persian digits
    org.junit.Assert.assertFalse(validationUtil.isValidPostalCode("198765432")) // 9 digits
    org.junit.Assert.assertFalse(validationUtil.isValidPostalCode("19876543210")) // 11 digits
    assertEquals("1987654321", validationUtil.cleanPostalCode("19876543219999")) // Takes 10
  }

  @Test
  fun `verify dashboard stat cards navigation to reports`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.InvoiceViewModel(app)

    // Total sales -> navigateToInvoiceList(0)
    viewModel.navigateToInvoiceList(0)
    assertEquals(0, viewModel.invoiceFilterTab.value)
    assertEquals(com.example.ui.Screen.InvoiceList, viewModel.currentScreen.value)

    // Paid invoices -> navigateToInvoiceList(1)
    viewModel.navigateToInvoiceList(1)
    assertEquals(1, viewModel.invoiceFilterTab.value)
    assertEquals(com.example.ui.Screen.InvoiceList, viewModel.currentScreen.value)

    // Credit invoices -> navigateToInvoiceList(2)
    viewModel.navigateToInvoiceList(2)
    assertEquals(2, viewModel.invoiceFilterTab.value)
    assertEquals(com.example.ui.Screen.InvoiceList, viewModel.currentScreen.value)

    // Customer count -> navigateTo(Screen.Customers)
    viewModel.navigateTo(com.example.ui.Screen.Customers)
    assertEquals(com.example.ui.Screen.Customers, viewModel.currentScreen.value)
  }

  @Test
  fun `verify about us dialog state`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.InvoiceViewModel(app)

    org.junit.Assert.assertFalse(viewModel.showAboutUs.value)
    viewModel.openAboutUs()
    org.junit.Assert.assertTrue(viewModel.showAboutUs.value)
    viewModel.closeAboutUs()
    org.junit.Assert.assertFalse(viewModel.showAboutUs.value)
  }
}

