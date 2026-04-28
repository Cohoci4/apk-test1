package com.foodenhancer.app.ui.screen

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.foodenhancer.app.R
import com.foodenhancer.app.ui.components.ShareBottomSheet
import com.foodenhancer.app.ui.theme.Primary
import com.foodenhancer.app.ui.viewmodel.ResultViewModel
import com.foodenhancer.core.util.StorageHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    onEnhanceAgain: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val isPro by viewModel.isPro.collectAsState()
    val context = LocalContext.current
    var showShareSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.result_title)) },
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
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            ComparisonSlider(
                originalUri = viewModel.originalUri,
                processedUri = viewModel.processedUri
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Primary, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = viewModel.styleName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!isPro) {
                Text(
                    text = stringResource(R.string.result_demo),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }

        if (isPro) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(Icons.Filled.Hd, contentDescription = "HD", tint = Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.result_hd), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = false,
                    onCheckedChange = { },
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (!StorageHelper.hasEnoughStorage()) {
                        Toast.makeText(context, context.getString(R.string.error_insufficient_storage), Toast.LENGTH_LONG).show()
                    } else {
                        saveImageToMediaStore(context, viewModel.processedUri)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.result_save))
            }

            OutlinedButton(
                onClick = { showShareSheet = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.result_share))
            }

            Button(
                onClick = onEnhanceAgain,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Again",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.result_again), color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showShareSheet) {
        ShareBottomSheet(
            imageFilePath = viewModel.processedUri,
            onDismiss = { showShareSheet = false }
        )
    }
}

private fun saveImageToMediaStore(context: Context, imageUri: String) {
    try {
        val file = File(imageUri)
        if (!file.exists()) {
            Toast.makeText(context, context.getString(R.string.error_image_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap == null) {
            Toast.makeText(context, context.getString(R.string.error_decode_failed), Toast.LENGTH_SHORT).show()
            return
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "FoodEnhancer_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FoodEnhancer")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                outputStream.use { os ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, os)
                }
                bitmap.recycle()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
                Toast.makeText(context, context.getString(R.string.result_saved), Toast.LENGTH_SHORT).show()
            } else {
                bitmap.recycle()
                context.contentResolver.delete(uri, null, null)
                Toast.makeText(context, context.getString(R.string.error_save_failed), Toast.LENGTH_SHORT).show()
            }
        } else {
            bitmap.recycle()
        }
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.error_save_failed), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ComparisonSlider(
    originalUri: String,
    processedUri: String
) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    var boxWidth by remember { mutableFloatStateOf(1f) }
    var boxHeight by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .onSizeChanged {
                boxWidth = it.width.toFloat()
                boxHeight = it.height.toFloat()
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val delta = dragAmount / boxWidth
                    sliderPosition = (sliderPosition + delta).coerceIn(0f, 1f)
                }
            }
    ) {
        AsyncImage(
            model = processedUri,
            contentDescription = "Processed",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(right = size.width * sliderPosition) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            AsyncImage(
                model = originalUri,
                contentDescription = "Original",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (boxWidth * sliderPosition).toInt() - with(density) { 1.dp.roundToPx() },
                        y = 0
                    )
                }
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.White)
        )

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (boxWidth * sliderPosition).toInt() - with(density) { 16.dp.roundToPx() },
                        y = (boxHeight * 0.5f).toInt() - with(density) { 16.dp.roundToPx() }
                    )
                }
                .size(32.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("◄►", fontSize = 10.sp, color = Color.Gray)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("Original", color = Color.White, fontSize = 10.sp)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("Enhanced", color = Color.White, fontSize = 10.sp)
        }
    }
}
