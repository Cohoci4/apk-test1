package com.foodenhancer.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodenhancer.app.R
import com.foodenhancer.app.ui.theme.Primary
import com.foodenhancer.core.util.SocialShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    imageFilePath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.share_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider()

            ShareOption(
                icon = Icons.Filled.Share,
                label = stringResource(R.string.share_image),
                color = Primary
            ) {
                val uri = SocialShareHelper.getContentUri(context, imageFilePath)
                if (uri != null) {
                    val intent = SocialShareHelper.createShareIntent(uri)
                    context.startActivity(android.content.Intent.createChooser(intent, null))
                }
                onDismiss()
            }

            ShareOption(
                icon = Icons.Filled.Image,
                label = stringResource(R.string.share_instagram),
                color = Color(0xFFE1306C)
            ) {
                if (SocialShareHelper.isAppInstalled(context, "com.instagram.android")) {
                    val uri = SocialShareHelper.getContentUri(context, imageFilePath)
                    if (uri != null) {
                        context.startActivity(SocialShareHelper.createInstagramFeedIntent(uri))
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.error_app_not_installed, "Instagram"), Toast.LENGTH_SHORT).show()
                }
                onDismiss()
            }

            ShareOption(
                icon = Icons.Filled.Camera,
                label = stringResource(R.string.share_instagram_stories),
                color = Color(0xFFC13584)
            ) {
                if (SocialShareHelper.isAppInstalled(context, "com.instagram.android")) {
                    val uri = SocialShareHelper.getContentUri(context, imageFilePath)
                    if (uri != null) {
                        try {
                            context.startActivity(SocialShareHelper.createInstagramStoriesIntent(context, uri))
                        } catch (_: Exception) {
                            context.startActivity(SocialShareHelper.createInstagramFeedIntent(uri))
                        }
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.error_app_not_installed, "Instagram"), Toast.LENGTH_SHORT).show()
                }
                onDismiss()
            }

            ShareOption(
                icon = Icons.Filled.Chat,
                label = stringResource(R.string.share_whatsapp),
                color = Color(0xFF25D366)
            ) {
                if (SocialShareHelper.isAppInstalled(context, "com.whatsapp")) {
                    val uri = SocialShareHelper.getContentUri(context, imageFilePath)
                    if (uri != null) {
                        context.startActivity(SocialShareHelper.createWhatsAppIntent(uri))
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.error_app_not_installed, "WhatsApp"), Toast.LENGTH_SHORT).show()
                }
                onDismiss()
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp)
    }
}
