package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEntry
import com.example.data.model.CampaignAngle
import com.example.ui.theme.MinimalAccentAmber
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalDarkBg
import com.example.ui.theme.MinimalPrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextLow
import com.example.ui.theme.TextMid
import com.example.ui.viewmodel.CreatorForgeViewModel
import com.example.ui.viewmodel.PlanSubTab

@Composable
fun PlanScreen(
    viewModel: CreatorForgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val subTab by viewModel.planSubTab.collectAsState()
    val calendarEntries by viewModel.generatedCalendar.collectAsState()
    val campaignAngles by viewModel.generatedCampaignAngles.collectAsState()

    val scheduledStates = remember(calendarEntries) {
        mutableStateListOf<Boolean>().apply {
            addAll(calendarEntries.map { false })
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBg)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Minimal Segmented Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MinimalSurface)
                .border(1.dp, MinimalBorder, RoundedCornerShape(20.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (subTab == PlanSubTab.CALENDAR) MinimalPrimary else Color.Transparent)
                    .clickable { viewModel.setPlanSubTab(PlanSubTab.CALENDAR) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Publishing Schedule",
                    fontSize = 12.sp,
                    fontWeight = if (subTab == PlanSubTab.CALENDAR) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (subTab == PlanSubTab.CALENDAR) Color.White else TextMid
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (subTab == PlanSubTab.CAMPAIGNS) MinimalPrimary else Color.Transparent)
                    .clickable { viewModel.setPlanSubTab(PlanSubTab.CAMPAIGNS) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A/B Angles",
                    fontSize = 12.sp,
                    fontWeight = if (subTab == PlanSubTab.CAMPAIGNS) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (subTab == PlanSubTab.CAMPAIGNS) Color.White else TextMid
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (subTab == PlanSubTab.CALENDAR) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("calendar_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(calendarEntries) { index, entry ->
                    val isChecked = scheduledStates.getOrElse(index) { false }
                    MinimalCalendarCard(
                        entry = entry,
                        isScheduled = isChecked,
                        onToggle = {
                            if (index < scheduledStates.size) scheduledStates[index] = !scheduledStates[index]
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Calendar Post", entry.sampleCopy))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("campaign_angles_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(campaignAngles) { angle ->
                    MinimalCampaignCard(
                        angle = angle,
                        onCopy = {
                            val text = "${angle.headline}\n\n${angle.hook}\n\n${angle.coreMessage}\n\nCTA: ${angle.callToAction}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Campaign Angle", text))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalCalendarCard(
    entry: CalendarEntry,
    isScheduled: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MinimalSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isScheduled) Color(0xFF10B981).copy(alpha = 0.5f) else MinimalBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.dayName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = TextHigh)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "• ${entry.timeSlot} • ${entry.platform}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextLow, fontSize = 11.sp)
                    )
                }

                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMid, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.sampleCopy,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMid, fontSize = 12.5.sp, lineHeight = 18.sp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isScheduled,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981), uncheckedColor = TextLow),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = if (isScheduled) "Published" else "Scheduled",
                    style = MaterialTheme.typography.bodySmall.copy(color = if (isScheduled) Color(0xFF10B981) else TextLow, fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
fun MinimalCampaignCard(
    angle: CampaignAngle,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MinimalSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = angle.angleName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = MinimalAccentAmber)
                )

                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMid, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = angle.headline,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextHigh)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = angle.coreMessage,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMid, fontSize = 12.sp, lineHeight = 17.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CTA: ${angle.callToAction}",
                style = MaterialTheme.typography.labelSmall.copy(color = MinimalPrimary, fontSize = 11.sp)
            )
        }
    }
}
