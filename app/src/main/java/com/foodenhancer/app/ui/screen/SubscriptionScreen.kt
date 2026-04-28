package com.foodenhancer.app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foodenhancer.app.ui.theme.Primary
import com.foodenhancer.app.ui.theme.SecondaryText
import com.foodenhancer.app.ui.viewmodel.SubscriptionViewModel

data class PlanInfo(
    val name: String,
    val price: String,
    val features: List<String>,
    val icon: ImageVector,
    val isCurrent: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val isPro by viewModel.isPro.collectAsState()
    val remainingDemo by viewModel.remainingDemo.collectAsState()
    val context = LocalContext.current

    val plans = listOf(
        PlanInfo(
            name = "Demo",
            price = "Free",
            features = listOf(
                "$remainingDemo/5 enhancements remaining",
                "Watermark on images",
                "Standard quality"
            ),
            icon = Icons.Filled.Star,
            isCurrent = !isPro
        ),
        PlanInfo(
            name = "Pro",
            price = "$9.99/mo",
            features = listOf(
                "Unlimited enhancements",
                "No watermark",
                "HD quality export"
            ),
            icon = Icons.Filled.Diamond,
            isCurrent = isPro
        ),
        PlanInfo(
            name = "Business",
            price = "$29.99/mo",
            features = listOf(
                "Everything in Pro",
                "API access",
                "Batch processing"
            ),
            icon = Icons.Filled.WorkspacePremium,
            isCurrent = false
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text("Subscription") })

        plans.forEach { plan ->
            PlanCard(plan = plan)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                Toast.makeText(context, "Google Play Billing coming soon", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Upgrade to Pro", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                Toast.makeText(context, "Restore coming soon", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Restore Purchases")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PlanCard(plan: PlanInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = if (plan.isCurrent) 2.dp else 1.dp,
                color = if (plan.isCurrent) Primary else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                if (plan.isCurrent) Primary.copy(alpha = 0.05f) else Color.White,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                plan.icon,
                contentDescription = null,
                tint = if (plan.isCurrent) Primary else SecondaryText,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = plan.price,
                    color = if (plan.isCurrent) Primary else SecondaryText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            if (plan.isCurrent) {
                Text(
                    text = "Current",
                    color = Primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        plan.features.forEach { feature ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (plan.isCurrent) Primary else SecondaryText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feature,
                    fontSize = 13.sp,
                    color = Color(0xFF444444)
                )
            }
        }
    }
}
