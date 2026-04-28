package com.foodenhancer.app.ui.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodenhancer.app.ui.theme.GradientEnd
import com.foodenhancer.app.ui.theme.GradientStart
import com.foodenhancer.app.ui.theme.Primary
import com.foodenhancer.app.ui.viewmodel.CategoryWithStyles
import com.foodenhancer.app.ui.viewmodel.StylePickerViewModel
import com.foodenhancer.domain.model.EnhancementStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylePickerScreen(
    onNavigateToResult: (String, String, String) -> Unit,
    onNavigateToCrop: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: StylePickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoriesWithStyles by viewModel.categoriesWithStyles.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    LaunchedEffect(uiState.navigateToCrop) {
        val styleId = uiState.navigateToCrop
        if (styleId != null) {
            onNavigateToCrop(Uri.encode(viewModel.imageUri), Uri.encode(styleId))
            viewModel.onCropNavigated()
        }
    }

    LaunchedEffect(uiState.resultOriginalUri) {
        val original = uiState.resultOriginalUri
        val processed = uiState.resultProcessedUri
        val style = uiState.resultStyleName
        if (original != null && processed != null && style != null) {
            onNavigateToResult(original, processed, style)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Choose a Style") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                categoriesWithStyles.forEach { categoryWithStyles ->
                    item(key = "header_${categoryWithStyles.category.id}") {
                        CategoryHeader(categoryWithStyles.category.name)
                    }
                    item(key = "row_${categoryWithStyles.category.id}") {
                        StyleRow(
                            styles = categoryWithStyles.styles,
                            selectedStyleId = uiState.selectedStyleId,
                            favoriteIds = favoriteIds,
                            onStyleClick = { viewModel.selectStyle(it) },
                            onFavoriteClick = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.onNext() },
                enabled = uiState.selectedStyleId != null && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Next",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingOverlay()
        }
    }
}

@Composable
fun CategoryHeader(name: String) {
    Text(
        text = name,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun StyleRow(
    styles: List<EnhancementStyle>,
    selectedStyleId: String?,
    favoriteIds: Set<String>,
    onStyleClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(styles, key = { it.id }) { style ->
            StyleCard(
                style = style,
                isSelected = selectedStyleId == style.id,
                isFavorite = style.id in favoriteIds,
                onClick = { onStyleClick(style.id) },
                onFavoriteClick = { onFavoriteClick(style.id) }
            )
        }
    }
}

@Composable
fun StyleCard(
    style: EnhancementStyle,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = tween(200),
        label = "styleScale"
    )

    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = tween(300),
        label = "heartScale"
    )

    val context = LocalContext.current
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
            .width(140.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .animateContentSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
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
                    Text(style.name, color = Color.White, fontSize = 12.sp)
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .clickable { onFavoriteClick() }
                    .scale(heartScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = style.name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Primary,
                modifier = Modifier.size(64.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Preparing magic...",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                AnimatedDots()
            }
        }
    }
}

@Composable
fun AnimatedDots() {
    val dots = listOf(0, 1, 2)
    dots.forEach { index ->
        val alpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(600),
            label = "dotAlpha$index"
        )
        Text(
            text = ".",
            color = Color.White.copy(alpha = 0.3f + (alpha * 0.7f * ((index + 1) / 3f))),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
