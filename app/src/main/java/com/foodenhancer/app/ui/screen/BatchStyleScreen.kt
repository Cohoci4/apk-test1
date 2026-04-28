package com.foodenhancer.app.ui.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.foodenhancer.app.ui.components.EnhanceLoadingOverlay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.foodenhancer.app.ui.theme.Primary
import com.foodenhancer.app.ui.viewmodel.BatchStyleViewModel
import com.foodenhancer.domain.model.EnhancementStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchStyleScreen(
    photoUris: List<String>,
    onComplete: (List<String>) -> Unit,
    onBack: () -> Unit,
    viewModel: BatchStyleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoriesWithStyles by viewModel.categoriesWithStyles.collectAsState()

    LaunchedEffect(photoUris) {
        viewModel.setPhotos(photoUris)
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete && uiState.resultUris.isNotEmpty()) {
            onComplete(uiState.resultUris)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Batch Enhancement (${photoUris.size} photos)") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        if (uiState.isProcessing) {
            val progress = uiState.progress
            val progressFraction = if (progress != null) {
                (progress.currentIndex + 1).toFloat() / progress.total
            } else null
            val message = if (progress != null) {
                "Processing ${progress.currentIndex + 1} of ${progress.total}"
            } else "Preparing"

            Box(modifier = Modifier.fillMaxSize()) {
                EnhanceLoadingOverlay(
                    visible = true,
                    progress = progressFraction,
                    message = message
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.isUniformMode,
                        onClick = { viewModel.setUniformMode(true) },
                        label = { Text("One style for all") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.2f)
                        )
                    )
                    FilterChip(
                        selected = !uiState.isUniformMode,
                        onClick = { viewModel.setUniformMode(false) },
                        label = { Text("Individual styles") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            if (uiState.isUniformMode) {
                item {
                    Text("Select a style for all photos:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                categoriesWithStyles.forEach { catWithStyles ->
                    item {
                        Text(
                            text = catWithStyles.category.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    item {
                        MiniStyleRow(
                            styles = catWithStyles.styles,
                            selectedStyleId = uiState.uniformStyleId,
                            onSelect = { viewModel.setUniformStyle(it) }
                        )
                    }
                }
            } else {
                itemsIndexed(photoUris) { index, uri ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = Uri.parse(uri),
                                contentDescription = "Photo ${index + 1}",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Photo ${index + 1}", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val allStyles = categoriesWithStyles.flatMap { it.styles }
                        MiniStyleRow(
                            styles = allStyles,
                            selectedStyleId = uiState.individualStyleIds[index],
                            onSelect = { viewModel.setIndividualStyle(index, it) }
                        )
                    }
                }
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        Button(
            onClick = { viewModel.enhanceAll() },
            enabled = !uiState.isProcessing && hasRequiredStyles(uiState),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Enhance All", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

private fun hasRequiredStyles(state: com.foodenhancer.app.ui.viewmodel.BatchStyleUiState): Boolean {
    return if (state.isUniformMode) {
        state.uniformStyleId != null
    } else {
        state.photoUris.indices.all { state.individualStyleIds.containsKey(it) }
    }
}

@Composable
fun MiniStyleRow(
    styles: List<EnhancementStyle>,
    selectedStyleId: String?,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(styles, key = { it.id }) { style ->
            val isSelected = selectedStyleId == style.id
            val previewBitmap = remember(style.previewUrl) {
                try {
                    context.assets.open(style.previewUrl).use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (_: Exception) {
                    null
                }
            }

            Column(
                modifier = Modifier
                    .width(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) Primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(style.id) }
                    .padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = style.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(style.name.take(3), color = Color.White, fontSize = 10.sp)
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(18.dp)
                                .background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                Text(
                    text = style.name,
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
