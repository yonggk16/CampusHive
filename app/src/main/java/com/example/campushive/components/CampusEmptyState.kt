package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.*

/**
 * Cardless empty-state indicator that sits directly on the screen background
 * without an outer Card or Surface wrapper.
 *
 * Uses a soft HoneyTint icon medallion ringed by a subtle dashed BrandHoney halo,
 * centered typography, an optional extra content slot, and an optional Honey pill CTA.
 */
@Composable
fun CampusEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    val haloColor = BrandHoney.copy(alpha = 0.55f)
    val haloSize: Dp = if (compact) 72.dp else 88.dp
    val medallionSize: Dp = if (compact) 56.dp else 70.dp
    val iconSize: Dp = if (compact) 23.dp else 28.dp
    val topPadding: Dp = if (compact) 16.dp else 28.dp
    val bottomPadding: Dp = if (compact) 16.dp else 24.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = topPadding, bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dashed BrandHoney Halo + HoneyTint Medallion + Accent Spark Dot
        Box(
            modifier = Modifier.size(haloSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = haloColor,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(4.dp.toPx(), 5.dp.toPx()),
                            phase = 0f
                        )
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(medallionSize)
                    .clip(CircleShape)
                    .background(HoneyTint)
                    .border(BorderStroke(1.2.dp, Hairline), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                    modifier = Modifier.size(iconSize)
                )
            }

            // Small restrained honey accent dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = if (compact) (-6).dp else (-8).dp, y = if (compact) 6.dp else 8.dp)
                    .size(if (compact) 6.dp else 8.dp)
                    .clip(CircleShape)
                    .background(BrandHoney)
            )
        }

        Spacer(Modifier.height(if (compact) 12.dp else 16.dp))

        Text(
            text = title,
            fontSize = if (compact) 15.sp else 17.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = subtitle,
            fontSize = if (compact) 12.sp else 13.sp,
            color = MutedText,
            textAlign = TextAlign.Center,
            lineHeight = if (compact) 17.sp else 19.sp
        )

        if (extraContent != null) {
            Spacer(Modifier.height(16.dp))
            extraContent()
        }

        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Honey,
                    contentColor = OnHoneyAccent
                ),
                shape = Radius.Pill,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.bounceClick(scaleDown = 0.94f, onClick = onAction)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
