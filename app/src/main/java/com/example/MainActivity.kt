package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.InvoiceViewModel
import com.example.ui.Screen
import com.example.ui.components.AboutUsDialog
import com.example.ui.components.AppDrawerContent
import com.example.ui.screens.BankAccountsScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoiceFormScreen
import com.example.ui.screens.InvoiceListScreen
import com.example.ui.screens.InvoiceViewScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SellerSettingsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.InvoiceAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                com.example.ui.ThemeMode.SYSTEM -> isSystemDark
                com.example.ui.ThemeMode.LIGHT -> false
                com.example.ui.ThemeMode.DARK -> true
            }
            val themeColor = viewModel.getActiveThemeColor()

            InvoiceAppTheme(
                darkTheme = isDark,
                accentColor = themeColor
            ) {
                // RTL layout support for Persian language
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: InvoiceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val sellerInfo by viewModel.sellerInfo.collectAsState()
    val showAboutUs by viewModel.showAboutUs.collectAsState()
    val themeColor = viewModel.getActiveThemeColor()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle back button on root/sub screens or close drawer if open
    BackHandler(enabled = drawerState.isOpen || currentScreen !is Screen.Dashboard) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.navigateBack()
        }
    }

    val isRootTab = currentScreen is Screen.Dashboard ||
            currentScreen is Screen.InvoiceList ||
            currentScreen is Screen.Products ||
            currentScreen is Screen.Customers ||
            currentScreen is Screen.BankAccounts ||
            currentScreen is Screen.SellerSettings ||
            currentScreen is Screen.Settings

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                sellerInfo = sellerInfo,
                themeColor = themeColor,
                onNavigate = { screen ->
                    coroutineScope.launch { drawerState.close() }
                    viewModel.navigateTo(screen)
                },
                onOpenAboutUs = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.openAboutUs()
                }
            )
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                val screensWithInternalTopBar = currentScreen is Screen.Settings ||
                        currentScreen is Screen.SellerSettings ||
                        currentScreen is Screen.BankAccounts ||
                        currentScreen is Screen.InvoiceForm ||
                        currentScreen is Screen.InvoiceView

                if (isRootTab && !screensWithInternalTopBar) {
                    TopAppBar(
                        title = {
                            val screenTitle = when (currentScreen) {
                                is Screen.Dashboard -> "داشبورد مدیریت"
                                is Screen.InvoiceList -> "فاکتورهای فروش"
                                is Screen.Products -> "مدیریت کالاها و انبار"
                                is Screen.Customers -> "مشتریان و طرف‌های حساب"
                                else -> "فاکتور پلاس"
                            }
                            Text(
                                text = screenTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            if (currentScreen is Screen.Dashboard) {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("btn_hamburger_menu")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "منوی ناوبری",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { viewModel.navigateBack() },
                                    modifier = Modifier.testTag("btn_top_back")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "بازگشت",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        actions = {
                            if (currentScreen !is Screen.Dashboard) {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("btn_hamburger_menu_sub")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "منوی ناوبری",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.openAboutUs() },
                                modifier = Modifier.testTag("btn_top_about_us")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "درباره ما",
                                    tint = themeColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (isRootTab) {
                    NavigationBar(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("main_bottom_nav"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { viewModel.navigateToTab(Screen.Dashboard) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "داشبورد") },
                            label = { Text("داشبورد") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = themeColor,
                                selectedTextColor = themeColor,
                                indicatorColor = themeColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_dashboard")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.InvoiceList,
                            onClick = { viewModel.navigateToTab(Screen.InvoiceList) },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "فاکتورها") },
                            label = { Text("فاکتورها") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = themeColor,
                                selectedTextColor = themeColor,
                                indicatorColor = themeColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_invoices")
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = { viewModel.navigateTo(Screen.InvoiceForm(null)) },
                            icon = {
                                Icon(
                                    Icons.Default.AddCircle,
                                    contentDescription = "صدور فاکتور",
                                    tint = themeColor
                                )
                            },
                            label = { Text("صدور") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = themeColor,
                                selectedTextColor = themeColor
                            ),
                            modifier = Modifier.testTag("nav_tab_new_invoice")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Products,
                            onClick = { viewModel.navigateToTab(Screen.Products) },
                            icon = { Icon(Icons.Default.Inventory2, contentDescription = "کالاها") },
                            label = { Text("کالاها") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = themeColor,
                                selectedTextColor = themeColor,
                                indicatorColor = themeColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_products")
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "منو") },
                            label = { Text("منو") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = themeColor,
                                selectedTextColor = themeColor,
                                indicatorColor = themeColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_menu")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentScreen) {
                    is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                    is Screen.InvoiceList -> InvoiceListScreen(viewModel = viewModel)
                    is Screen.InvoiceForm -> InvoiceFormScreen(invoiceId = (currentScreen as Screen.InvoiceForm).invoiceId, viewModel = viewModel)
                    is Screen.InvoiceView -> InvoiceViewScreen(invoiceId = (currentScreen as Screen.InvoiceView).invoiceId, viewModel = viewModel)
                    is Screen.Customers -> CustomersScreen(viewModel = viewModel)
                    is Screen.Products -> ProductsScreen(viewModel = viewModel)
                    is Screen.BankAccounts -> BankAccountsScreen(viewModel = viewModel)
                    is Screen.SellerSettings -> SellerSettingsScreen(viewModel = viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // About Us Dialog
    if (showAboutUs) {
        AboutUsDialog(
            themeColor = themeColor,
            onDismissRequest = { viewModel.closeAboutUs() }
        )
    }
}
