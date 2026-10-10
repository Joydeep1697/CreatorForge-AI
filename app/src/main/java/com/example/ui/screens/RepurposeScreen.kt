package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalDarkBg
import com.example.ui.theme.MinimalPrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextLow
import com.example.ui.theme.TextMid
import com.example.ui.viewmodel.CreatorForgeViewModel

@Composable
fun RepurposeScreen(
    viewModel: CreatorForgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawContent by viewModel.repurposeRawContent.collectAsState()
    val selectedChannels by viewModel.selectedChannels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bundle by viewModel.repurposedBundle.collectAsState()

    var selectedTabOutput by remember { mutableIntStateOf(0) }

    val availableChannels = listOf(
        "LinkedIn",
        "Twitter/X Thread",
        "Instagram Carousel",
        "Short Video Script",
        "Email Newsletter"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Minimal Prompt Card for Original Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MinimalSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = rawContent,
                    onValueChange = { viewModel.setRepurposeContent(it) },
                    placeholder = {
                        Text(
                            text = "Paste a blog post, video script, or draft to adapt for multiple platforms...",
                            color = TextLow,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repurpose_input_field"),
                    minLines = 5,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextHigh,
                        unfocusedTextColor = TextHigh,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Target Channels Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableChannels.forEach { channel ->
                        val isSelected = selectedChannels.contains(channel)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MinimalPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (isSelected) MinimalPrimary else MinimalBorder, RoundedCornerShape(16.dp))
                                .clickable { viewModel.toggleChannel(channel) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = channel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else TextMid,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Execute Button
        Button(
            onClick = { viewModel.executeRepurpose() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("repurpose_execute_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MinimalPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Repurposing...", fontSize = 14.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Repurpose Content (5 credits)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Repurposed Results
        if (bundle != null && !isLoading) {
            Spacer(modifier = Modifier.height(20.dp))

            val outputTabs = listOf("LinkedIn", "X Thread", "Carousel", "Video", "Newsletter")

            TabRow(
                selectedTabIndex = selectedTabOutput,
                containerColor = MinimalSurface,
                contentColor = MinimalPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabOutput]),
                        color = MinimalPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MinimalBorder, RoundedCornerShape(10.dp))
            ) {
                outputTabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedTabOutput == index,
                        onClick = { selectedTabOutput = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTabOutput == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTabOutput == index) Color.White else TextMid
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentContent = when (selectedTabOutput) {
                0 -> bundle!!.linkedinPost
                1 -> bundle!!.twitterThread.joinToString("\n\n")
                2 -> bundle!!.instagramCarousel.joinToString("\n\n")
                3 -> bundle!!.shortVideoScript
                else -> bundle!!.newsletterDraft
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinimalSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = outputTabs[selectedTabOutput],
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextHigh
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Repurposed", currentContent))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMid, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, currentContent)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share"))
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextMid, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentContent,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextHigh,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
