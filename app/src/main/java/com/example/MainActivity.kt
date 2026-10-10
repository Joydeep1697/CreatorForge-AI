package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AccountDialog
import com.example.ui.components.AudienceTargetingSheet
import com.example.ui.components.CreatorTopBar
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MonetizationDialog
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.RepurposeScreen
import com.example.ui.screens.StudioForgeScreen
import com.example.ui.theme.CreatorForgeTheme
import com.example.ui.theme.MinimalDarkBg
import com.example.ui.theme.MinimalPrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.TextLow
import com.example.ui.viewmodel.CreatorForgeViewModel
import com.example.ui.viewmodel.WorkspaceTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreatorForgeTheme {
                CreatorForgeApp()
            }
        }
    }
}

@Composable
fun CreatorForgeApp(
    viewModel: CreatorForgeViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val tier by viewModel.tier.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val showPaywall by viewModel.showPaywallDialog.collectAsState()
    val showAudiencePicker by viewModel.showAudiencePicker.collectAsState()
    val showAccountDialog by viewModel.showAccountDialog.collectAsState()
    val selectedAudience by viewModel.selectedAudience.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = currentTab != WorkspaceTab.STUDIO_FORGE) {
        viewModel.setTab(WorkspaceTab.STUDIO_FORGE)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("creator_forge_scaffold"),
        containerColor = MinimalDarkBg,
        topBar = {
            CreatorTopBar(
                credits = credits,
                tier = tier,
                currentUser = currentUser,
                onOpenPaywall = { viewModel.setShowPaywall(true) },
                onOpenAccount = { viewModel.setShowAccountDialog(true) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MinimalSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == WorkspaceTab.STUDIO_FORGE,
                    onClick = { viewModel.setTab(WorkspaceTab.STUDIO_FORGE) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Create") },
                    label = { Text("Create", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = MinimalPrimary,
                        unselectedIconColor = TextLow,
                        unselectedTextColor = TextLow
                    )
                )

                NavigationBarItem(
                    selected = currentTab == WorkspaceTab.REPURPOSE,
                    onClick = { viewModel.setTab(WorkspaceTab.REPURPOSE) },
                    icon = { Icon(Icons.Default.Repeat, contentDescription = "Repurpose") },
                    label = { Text("Repurpose", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = MinimalPrimary,
                        unselectedIconColor = TextLow,
                        unselectedTextColor = TextLow
                    )
                )

                NavigationBarItem(
                    selected = currentTab == WorkspaceTab.PLAN,
                    onClick = { viewModel.setTab(WorkspaceTab.PLAN) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Plan") },
                    label = { Text("Plan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = MinimalPrimary,
                        unselectedIconColor = TextLow,
                        unselectedTextColor = TextLow
                    )
                )

                NavigationBarItem(
                    selected = currentTab == WorkspaceTab.LIBRARY,
                    onClick = { viewModel.setTab(WorkspaceTab.LIBRARY) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Library") },
                    label = { Text("Library", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = MinimalPrimary,
                        unselectedIconColor = TextLow,
                        unselectedTextColor = TextLow
                    )
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            WorkspaceTab.STUDIO_FORGE -> StudioForgeScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            WorkspaceTab.REPURPOSE -> RepurposeScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            WorkspaceTab.PLAN -> PlanScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            WorkspaceTab.LIBRARY -> LibraryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    // Account & Google Sign-In Dialog
    if (showAccountDialog) {
        AccountDialog(
            currentUser = currentUser,
            tier = tier,
            credits = credits,
            onSignIn = { act -> viewModel.signInWithGoogle(act) },
            onSignOut = { viewModel.signOut(context) },
            onDismiss = { viewModel.setShowAccountDialog(false) }
        )
    }

    // Paywall & Credits Dialog
    if (showPaywall) {
        MonetizationDialog(
            currentTier = tier,
            currentCredits = credits,
            onUpgradeTier = { newTier -> viewModel.upgradeTier(newTier) },
            onBuyCredits = { added -> viewModel.buyCredits(added) },
            onDismiss = { viewModel.setShowPaywall(false) }
        )
    }

    // Audience Targeting Sheet
    if (showAudiencePicker) {
        AudienceTargetingSheet(
            selectedAudience = selectedAudience,
            onSelectAudience = { audience -> viewModel.setAudience(audience) },
            onDismiss = { viewModel.setShowAudiencePicker(false) }
        )
    }
}
