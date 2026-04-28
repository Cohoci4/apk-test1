package com.foodenhancer.app.ui.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodenhancer.app.ui.theme.Primary
import com.foodenhancer.app.ui.viewmodel.CropRotateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRotateScreen(
    onNavigateToResult: (String, String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: CropRotateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.resultOriginalUri) {
        val original = uiState.resultOriginalUri
        val processed = uiState.resultProcessedUri
        val style = uiState.resultStyleName
        if (original != null && processed != null && style != null) {
            onNavigateToResult(original, processed, style)
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(uiState.rotation) {
        rotation = uiState.rotation
    }

    val transformState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        rotation += rotationChange
        offset += panChange
        viewModel.updateTransform(rotation, scale, offset)
    }

    val previewBitmap = remember(viewModel.imageUri) {
        try {
            when {
                viewModel.imageUri.startsWith("content://") -> {
                    val parsedUri = Uri.parse(viewModel.imageUri)
                    context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                viewModel.imageUri.startsWith("file:///android_asset/") -> {
                    val assetPath = viewModel.imageUri.removePrefix("file:///android_asset/")
                    context.assets.open(assetPath).use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                viewModel.imageUri.startsWith("/") -> BitmapFactory.decodeFile(viewModel.imageUri)
                else -> {
                    val parsedUri = Uri.parse(viewModel.imageUri)
                    context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Crop & Rotate") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(2.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Image to crop",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .transformable(state = transformState)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                rotationZ = rotation,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                } else {
                    Text("Loading image...", color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color.White.copy(alpha = 0.6f))
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = { viewModel.rotate90() }) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate 90°")
            }

            OutlinedButton(
                onClick = {
                    scale = 1f
                    rotation = 0f
                    offset = Offset.Zero
                    viewModel.reset()
                }
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.applyAndEnhance() },
            enabled = !uiState.isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processing...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text("Apply & Enhance", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
