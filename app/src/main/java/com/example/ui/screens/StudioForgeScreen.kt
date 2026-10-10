package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TargetAudience
import com.example.ui.components.AudienceTargetingSheet
import com.example.ui.theme.MinimalAccentAmber
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalDarkBg
import com.example.ui.theme.MinimalPrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.MinimalSurfaceHigh
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextLow
import com.example.ui.theme.TextMid
import com.example.ui.viewmodel.CreatorForgeViewModel
import com.example.ui.viewmodel.StudioFormat

@Composable
fun StudioForgeScreen(
    viewModel: CreatorForgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val audience by viewModel.selectedAudience.collectAsState()
    val tone by viewModel.selectedTone.collectAsState()
    val ideaInput by viewModel.ideaInput.collectAsState()
    val selectedBitmap by viewModel.selectedImageBitmap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val generatedResult by viewModel.generatedResult.collectAsState()
    val showAudiencePicker by viewModel.showAudiencePicker.collectAsState()

    var showToneMenu by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ -> decoder.isMutableRequired = true }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.setSelectedImage(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val tones = listOf(
        "Direct & High Value",
        "Energetic & Viral",
        "Professional & Authoritative",
        "Witty & Relatable",
        "Empathetic & Story"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Minimalist Format Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudioFormat.values().forEach { format ->
                val isSelected = selectedFormat == format
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MinimalPrimary else MinimalSurface)
                        .border(1.dp, if (isSelected) MinimalPrimary else MinimalBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setSelectedFormat(format) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = format.displayName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextMid,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Minimalist Prompt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MinimalSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = ideaInput,
                    onValueChange = { viewModel.setIdeaInput(it) },
                    placeholder = {
                        Text(
                            text = "What are you launching or creating? Add product details or core offer...",
                            color = TextLow,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("idea_input_field"),
                    minLines = 4,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextHigh,
                        unfocusedTextColor = TextHigh,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                // Attached Photo Preview
                if (selectedBitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MinimalSurfaceHigh)
                            .padding(6.dp)
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Attached photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Product photo attached",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMid, fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.setSelectedImage(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = TextMid, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Inline Controls: Audience & Tone & Photo Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Audience Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .testTag("audience_targeting_section")
                                .clip(RoundedCornerShape(8.dp))
                                .background(MinimalSurfaceHigh)
                                .clickable { viewModel.setShowAudiencePicker(true) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = audience.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMid,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextMid,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Tone Chip
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MinimalSurfaceHigh)
                                    .clickable { showToneMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tone.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMid,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextMid,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showToneMenu,
                                onDismissRequest = { showToneMenu = false }
                            ) {
                                tones.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.setTone(t)
                                            showToneMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Attach Photo Icon Button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach photo",
                            tint = if (selectedBitmap != null) MinimalPrimary else TextMid,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Single Primary Action Button
        Button(
            onClick = { viewModel.forgeContent() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("forge_social_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MinimalPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creating...", fontSize = 14.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Forge ${selectedFormat.displayName} (${selectedFormat.creditCost} credits)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Generated Result Display
        if (generatedResult != null && !isLoading) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generated_result_card"),
                colors = CardDefaults.cardColors(containerColor = MinimalSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Content",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextHigh
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("CreatorForge", generatedResult))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMid, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, generatedResult)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = generatedResult!!,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextHigh,
                            fontSize = 13.5.sp,
                            lineHeight = 21.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showAudiencePicker) {
        AudienceTargetingSheet(
            selectedAudience = audience,
            onSelectAudience = { viewModel.setAudience(it) },
            onDismiss = { viewModel.setShowAudiencePicker(false) }
        )
    }
}
