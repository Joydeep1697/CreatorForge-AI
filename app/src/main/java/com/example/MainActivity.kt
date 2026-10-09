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
import androidx.compose.material.icons.filled.ViewCarousel
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AudienceTargetingSheet
import com.example.ui.components.CreatorTopBar
import com.example.ui.screens.CampaignVariationsScreen
import com.example.ui.screens.ContentCalendarScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MonetizationDialog
import com.example.ui.screens.RepurposeScreen
import com.example.ui.screens.StudioForgeScreen
import com.example.ui.theme.CreatorForgeTheme
import com.example.ui.theme.ForgeDarkBg
import com.example.ui.theme.ForgeDarkSurface
import com.example.ui.theme.ForgeVioletPrimary
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
    val showPaywall by viewModel.showPaywallDialog.collectAsState()
    val showAudiencePicker by viewModel.showAudiencePicker.collectAsState()
    val selectedAudience by viewModel.selectedAudience.collectAsState()

    // BackHandler to return to StudioForge if on another tab
    BackHandler(enabled = currentTab != WorkspaceTab.STUDIO_FORGE) {
        viewModel.setTab(WorkspaceTab.STUDIO_FORGE)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("creator_forge_scaffold"),
        containerColor = ForgeDarkBg,
        topBar = {
            CreatorTopBar(
                credits = credits,
                tier = tier,
                onOpenPaywall = { viewModel.setShowPaywall(true) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ForgeDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == WorkspaceTab.STUDIO_FORGE,
                    onClick = { viewModel.setTab(WorkspaceTab.STUDIO_FORGE) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Studio") },
                    label = { Text("Studio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = ForgeVioletPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
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
                        indicatorColor = ForgeVioletPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == WorkspaceTab.CALENDAR,
                    onClick = { viewModel.setTab(WorkspaceTab.CALENDAR) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = ForgeVioletPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == WorkspaceTab.CAMPAIGNS,
                    onClick = { viewModel.setTab(WorkspaceTab.CAMPAIGNS) },
                    icon = { Icon(Icons.Default.ViewCarousel, contentDescription = "Campaigns") },
                    label = { Text("Campaigns", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = ForgeVioletPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
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
                        indicatorColor = ForgeVioletPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
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
            WorkspaceTab.CALENDAR -> ContentCalendarScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            WorkspaceTab.CAMPAIGNS -> CampaignVariationsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            WorkspaceTab.LIBRARY -> LibraryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
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

    // Audience Targeting Sheet if requested from anywhere
    if (showAudiencePicker) {
        AudienceTargetingSheet(
            selectedAudience = selectedAudience,
            onSelectAudience = { audience -> viewModel.setAudience(audience) },
            onDismiss = { viewModel.setShowAudiencePicker(false) }
        )
    }
}
