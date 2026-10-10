package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSubscriptionTier
import com.example.ui.theme.MinimalAccentAmber
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalDarkBg
import com.example.ui.theme.MinimalPrimary
import com.example.ui.theme.MinimalSurfaceHigh
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMid
import com.google.firebase.auth.FirebaseUser

@Composable
fun CreatorTopBar(
    credits: Int,
    tier: UserSubscriptionTier,
    currentUser: FirebaseUser?,
    onOpenPaywall: () -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MinimalDarkBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean Brand Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MinimalPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CreatorForge",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.3).sp,
                        color = TextHigh
                    )
                )
            }

            // Right Actions: Minimalist Credits Pill & Account
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Credits Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .testTag("credits_pill")
                        .clip(RoundedCornerShape(20.dp))
                        .background(MinimalSurfaceHigh)
                        .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp))
                        .clickable { onOpenPaywall() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Credits",
                        tint = MinimalAccentAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$credits",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextHigh,
                            fontSize = 12.sp
                        )
                    )
                }

                // Account / Google Sign-In Avatar
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MinimalSurfaceHigh)
                        .border(1.dp, MinimalBorder, CircleShape)
                        .clickable { onOpenAccount() },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentUser != null) {
                        Text(
                            text = (currentUser.displayName?.take(1) ?: currentUser.email?.take(1) ?: "U").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MinimalPrimary,
                                fontSize = 12.sp
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = TextMid,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
