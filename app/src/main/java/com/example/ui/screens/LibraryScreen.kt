package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItem
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

@Composable
fun LibraryScreen(
    viewModel: CreatorForgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedItems by viewModel.savedItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = savedItems.filter { item ->
        searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.content.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBg)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Cloud Sync Banner (if not signed in)
        if (currentUser == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MinimalSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Sync",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextHigh)
                        )
                        Text(
                            text = "Sign in to backup creations to Firebase",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextLow, fontSize = 11.sp)
                        )
                    }

                    Button(
                        onClick = {
                            (context as? Activity)?.let { act ->
                                viewModel.signInWithGoogle(act)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("library_google_sign_in_btn")
                    ) {
                        Text("Sign In", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Minimalist Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search creations...", color = TextLow, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextLow, modifier = Modifier.size(16.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MinimalBorder,
                unfocusedBorderColor = MinimalBorder,
                focusedTextColor = TextHigh,
                unfocusedTextColor = TextHigh,
                focusedContainerColor = MinimalSurface,
                unfocusedContainerColor = MinimalSurface
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (savedItems.isEmpty()) "No saved creations yet" else "No matches found",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextLow)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("saved_items_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    MinimalSavedItemCard(
                        item = item,
                        onToggleFavorite = { viewModel.toggleFavorite(item) },
                        onDelete = { viewModel.deleteItem(item) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Creation", item.content))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, item.content)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalSavedItemCard(
    item: ContentItem,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
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
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = TextHigh),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) MinimalAccentAmber else TextLow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMid, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextMid, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextLow, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.content,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMid, fontSize = 12.sp, lineHeight = 17.sp),
                maxLines = 4
            )
        }
    }
}
