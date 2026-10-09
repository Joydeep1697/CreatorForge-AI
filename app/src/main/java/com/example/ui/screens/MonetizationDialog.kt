package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserSubscriptionTier
import com.example.ui.theme.ForgeAmberAccent
import com.example.ui.theme.ForgeCoralAccent
import com.example.ui.theme.ForgeDarkSurface
import com.example.ui.theme.ForgeDarkSurfaceBorder
import com.example.ui.theme.ForgeVioletPrimary

@Composable
fun MonetizationDialog(
    currentTier: UserSubscriptionTier,
    currentCredits: Int,
    onUpgradeTier: (UserSubscriptionTier) -> Unit,
    onBuyCredits: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ForgeDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ForgeDarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ForgeAmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Credits & Workspace Plans",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    text = "You currently have $currentCredits credits in ${currentTier.displayName}.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 12.sp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Quick Free Top-up for Testing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎁 Creator Daily Bonus",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Instantly add +25 free generation credits",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Button(
                            onClick = { onBuyCredits(25) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeAmberAccent),
                            modifier = Modifier.testTag("claim_free_credits_button")
                        ) {
                            Text(
                                text = "+25 Free",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Subscription Tiers",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Creator Pro Card
                SubscriptionTierCard(
                    tier = UserSubscriptionTier.PRO,
                    isCurrent = currentTier == UserSubscriptionTier.PRO,
                    features = listOf(
                        "1,000 Generation Credits / month",
                        "Unlimited Content Repurposing",
                        "Advanced Audience Demographic Tuning",
                        "Priority Gemini 3.5 Flash Speed"
                    ),
                    onSelect = { onUpgradeTier(UserSubscriptionTier.PRO) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Agency Scale Card
                SubscriptionTierCard(
                    tier = UserSubscriptionTier.AGENCY,
                    isCurrent = currentTier == UserSubscriptionTier.AGENCY,
                    features = listOf(
                        "5,000 Generation Credits / month",
                        "Unlimited Campaign Variations",
                        "Multi-Brand Voice & Client Export",
                        "Zero Generation Credit Restrictions"
                    ),
                    onSelect = { onUpgradeTier(UserSubscriptionTier.AGENCY) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Credit Top-Up Packs",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CreditPackCard(
                        amount = 50,
                        price = "$4.99",
                        modifier = Modifier.weight(1f),
                        onClick = { onBuyCredits(50) }
                    )
                    CreditPackCard(
                        amount = 200,
                        price = "$14.99",
                        isPopular = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onBuyCredits(200) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionTierCard(
    tier: UserSubscriptionTier,
    isCurrent: Boolean,
    features: List<String>,
    onSelect: () -> Unit
) {
    val borderColor = if (isCurrent) ForgeVioletPrimary else ForgeDarkSurfaceBorder
    val bg = if (isCurrent) Color(0xFF1E1B4B) else Color(0xFF131D2E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tier.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = tier.price,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = ForgeAmberAccent,
                            fontSize = 15.sp
                        )
                    )
                }

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ForgeVioletPrimary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CURRENT PLAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeVioletPrimary)
                    ) {
                        Text("Upgrade")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feat,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CreditPackCard(
    amount: Int,
    price: String,
    isPopular: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isPopular) {
                Text(
                    text = "MOST POPULAR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ForgeCoralAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                )
            }
            Text(
                text = "+$amount Credits",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = price,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ForgeAmberAccent
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Text("Add", fontSize = 12.sp)
            }
        }
    }
}
