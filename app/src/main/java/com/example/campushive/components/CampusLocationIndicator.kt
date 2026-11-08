package com.example.campushive.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.Charcoal
import com.example.campushive.ui.theme.CreamTint
import com.example.campushive.ui.theme.Hairline
import com.example.campushive.ui.theme.MutedText
import com.example.campushive.ui.theme.OnlineGreen
import com.example.campushive.ui.theme.Radius
import com.example.campushive.ui.theme.bounceClick

/**
 * Seamless campus location indicator used by the auth screens (login + signup).
 *
 * Design intent: reads as part of the header, not a separate badge.
 * - Soft cream "tinted glass" surface instead of a hard white card, so it sits
 *   in the background rather than floating above it.
 * - A single live pulse dot replaces the old dot + icon + bold text stack:
 *   one signal, one focus.
 * - Hairline border at low opacity + tight elevation keeps the edge soft.
 * - Semi-bold (not bold) text so the logo stays the anchor of the row.
 *
 * @param campusName label shown in the capsule, e.g. "HELP Subang 2 Campus".
 * @param onClick optional tap handler; when null the capsule is non-interactive.
 */
@Composable
fun CampusLocationIndicator(
    campusName: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    // Slow, subtle "we are live here" pulse — never competes for attention.
    val pulse = rememberInfiniteTransition(label = "locationPulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 2.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Restart
        ),
        label = "locationPulseScale"
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Restart
        ),
        label = "locationPulseAlpha"
    )

    Surface(
        modifier = modifier.bounceClick(scaleDown = 0.97f, onClick = onClick),
        shape = Radius.Pill,
        color = CreamTint.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Hairline.copy(alpha = 0.7f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 11.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live pulse dot
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .clip(CircleShape)
                        .background(OnlineGreen)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(OnlineGreen)
                )
            }

            Spacer(Modifier.width(7.dp))

            Text(
                text = campusName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal.copy(alpha = 0.88f),
                letterSpacing = 0.1.sp,
                maxLines = 1,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.04f),
                        offset = Offset(0f, 1f),
                        blurRadius = 1.5f
                    )
                )
            )

            Spacer(Modifier.width(2.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MutedText.copy(alpha = 0.75f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
