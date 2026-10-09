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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.getAudienceIcon
import com.example.ui.theme.ForgeAmberAccent
import com.example.ui.theme.ForgeDarkSurface
import com.example.ui.theme.ForgeDarkSurfaceBorder
import com.example.ui.theme.ForgeVioletPrimary
import com.example.ui.viewmodel.CreatorForgeViewModel

@Composable
fun RepurposeScreen(
    viewModel: CreatorForgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawContent by viewModel.repurposeRawContent.collectAsState()
    val tone by viewModel.repurposeTone.collectAsState()
    val audience by viewModel.selectedAudience.collectAsState()
    val selectedChannels by viewModel.selectedChannels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val bundle by viewModel.repurposedBundle.collectAsState()

    var selectedTabOutput by remember { mutableIntStateOf(0) }

    val availableChannels = listOf(
        "LinkedIn",
        "Twitter/X Thread",
        "Instagram Carousel",
        "Short Video Script",
        "Email Newsletter"
    )

    val tones = listOf(
        "Conversational & Authentic",
        "Professional & Authoritative",
        "Witty & Casual",
        "High-Energy Hype",
        "Educational Step-by-Step"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF3B0764)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = ForgeVioletPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Content Repurposing Hub",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Transform 1 long-form post into 5+ channel-native variations",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target Demographic Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setShowAudiencePicker(true) },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getAudienceIcon(audience.iconName),
                        contentDescription = null,
                        tint = ForgeVioletPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audience: ${audience.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ForgeAmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Sample Loader Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Load template: ",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            OutlinedButton(
                onClick = {
                    viewModel.setRepurposeContent(
                        """
                        Most creators burn out because they treat every platform like a fresh blank page. In reality, the top 1% of content engines use a hub-and-spoke model: write one deep-dive piece of content per week, and systematically dissect it into high-intent short posts, threads, carousels, and emails. By tailoring the tone and hooks to your exact demographic, you compound your reach with 80% less effort.
                        """.trimIndent()
                    )
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Sample: Content Engine", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    viewModel.setRepurposeContent(
                        """
                        Today we announced our sustainable carbon-negative packaging line. Unlike standard plastics that take 450 years to degrade, our plant-fiber pods naturally dissolve in 60 days leaving zero microplastics. We spent 14 months perfecting the barrier seal so your coffee stays fresh without petrochemical coatings.
                        """.trimIndent()
                    )
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Sample: Eco Launch", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Raw Content Input
        OutlinedTextField(
            value = rawContent,
            onValueChange = { viewModel.setRepurposeContent(it) },
            label = { Text("Original Content (Blog, Video Transcript, Memo, Notes)") },
            placeholder = { Text("Paste your long-form text here to transform it across all channels...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("repurpose_input_field"),
            minLines = 4,
            maxLines = 8,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForgeVioletPrimary,
                unfocusedBorderColor = ForgeDarkSurfaceBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1A2234),
                unfocusedContainerColor = Color(0xFF1A2234)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Target Channels Checkboxes
        Text(
            text = "Target Channels to Generate",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            availableChannels.forEach { channel ->
                val isSelected = selectedChannels.contains(channel)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleChannel(channel) },
                    label = { Text(channel, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForgeVioletPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF131D2E),
                        labelColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tone Adaptation
        Text(
            text = "Tone Adaptation",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tones.forEach { t ->
                FilterChip(
                    selected = tone == t,
                    onClick = { viewModel.setRepurposeTone(t) },
                    label = { Text(t, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4C1D95),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF131D2E),
                        labelColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Execute Repurpose Button
        Button(
            onClick = { viewModel.executeRepurpose() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("repurpose_execute_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ForgeVioletPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Repurpose Across Channels (5 credits)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = ForgeVioletPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = statusMessage ?: "Synthesizing cross-channel formats...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
        }

        // Repurposed Results Display
        if (bundle != null && !isLoading) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Repurposed Channel Variations",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            val outputTabs = listOf("LinkedIn", "X Thread", "Carousel", "Video Script", "Newsletter", "Takeaways")

            TabRow(
                selectedTabIndex = selectedTabOutput,
                containerColor = Color(0xFF111827),
                contentColor = ForgeVioletPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabOutput]),
                        color = ForgeVioletPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                outputTabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedTabOutput == index,
                        onClick = { selectedTabOutput = index },
                        text = { Text(tabTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTabOutput) {
                0 -> RepurposedChannelCard(
                    title = "💼 LinkedIn Leadership Post",
                    content = bundle!!.linkedinPost,
                    channelTag = "LinkedIn"
                )
                1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bundle!!.twitterThread.forEachIndexed { i, tweet ->
                        RepurposedChannelCard(
                            title = "🐦 Tweet ${i + 1} of ${bundle!!.twitterThread.size}",
                            content = tweet,
                            channelTag = "Twitter/X"
                        )
                    }
                }
                2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bundle!!.instagramCarousel.forEachIndexed { i, slide ->
                        RepurposedChannelCard(
                            title = "📸 Carousel Slide ${i + 1}",
                            content = slide,
                            channelTag = "Instagram"
                        )
                    }
                }
                3 -> RepurposedChannelCard(
                    title = "🎬 Short Video Script (30-60s)",
                    content = bundle!!.shortVideoScript,
                    channelTag = "Shorts/Reels"
                )
                4 -> RepurposedChannelCard(
                    title = "✉️ Email Newsletter Edition",
                    content = bundle!!.newsletterDraft,
                    channelTag = "Newsletter"
                )
                5 -> RepurposedChannelCard(
                    title = "💡 Key Takeaways Pack",
                    content = bundle!!.keyTakeaways.joinToString("\n• ", prefix = "• "),
                    channelTag = "Takeaways"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RepurposedChannelCard(
    title: String,
    content: String,
    channelTag: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeDarkSurfaceBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Row {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("CreatorForge Repurpose", content))
                        Toast.makeText(context, "Copied $channelTag to clipboard!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray)
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, content)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share $channelTag"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D16))
                    .padding(12.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                )
            }
        }
    }
}
