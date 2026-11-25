package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.*

data class CampusSticker(
    val id: String,
    val emoji: String,
    val label: String,
    val bgColor: Color,
    val accentColor: Color
)

object CampusStickerPack {
    val stickers: List<CampusSticker> = listOf(
        CampusSticker("on_my_way", "\uD83C\uDFC3", "On my way!", Color(0xFFFFF3CC), Color(0xFFE0A800)),
        CampusSticker("deal", "\uD83E\uDD1D", "Deal!", Color(0xFFE6F7EE), Color(0xFF1F8A5B)),
        CampusSticker("thanks", "\uD83D\uDE4F", "Thanks a ton!", Color(0xFFFFEFE6), Color(0xFFD96B27)),
        CampusSticker("study_mode", "\uD83D\uDCDA", "Study mode", Color(0xFFEAF1FF), Color(0xFF3563E9)),
        CampusSticker("coffee_first", "\u2615", "Coffee first", Color(0xFFF5ECE4), Color(0xFF8B5A2B)),
        CampusSticker("good_luck", "\uD83C\uDF40", "Good luck!", Color(0xFFE8F8EA), Color(0xFF2E9E47)),
        CampusSticker("got_it", "\uD83D\uDC4D", "Got it!", Color(0xFFFFF3CC), Color(0xFFD99B00)),
        CampusSticker("see_you", "\uD83D\uDC4B", "See you soon", Color(0xFFF3ECFF), Color(0xFF7C4DFF)),
        CampusSticker("awesome", "\uD83D\uDD25", "Awesome work!", Color(0xFFFFEAE8), Color(0xFFD94136)),
        CampusSticker("need_help", "\uD83C\uDD98", "Send help!", Color(0xFFFFEBF0), Color(0xFFD83467)),
        CampusSticker("all_done", "\u2705", "All done!", Color(0xFFE6F7EE), Color(0xFF1F8A5B)),
        CampusSticker("five_stars", "\u2B50", "5 Stars!", Color(0xFFFFF5D1), Color(0xFFD49200))
    )

    fun findById(id: String): CampusSticker? = stickers.find { it.id == id }
}

@Composable
fun CampusStickerCard(
    sticker: CampusSticker,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    size: Dp = if (compact) 84.dp else 132.dp
) {
    Surface(
        shape = RoundedCornerShape(if (compact) 16.dp else 22.dp),
        color = sticker.bgColor,
        border = BorderStroke(1.5.dp, sticker.accentColor.copy(alpha = 0.35f)),
        shadowElevation = if (compact) 0.dp else 2.dp,
        modifier = modifier.width(size)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 6.dp else 12.dp,
                    vertical = if (compact) 8.dp else 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = sticker.emoji,
                fontSize = if (compact) 26.sp else 42.sp
            )
            Spacer(Modifier.height(if (compact) 3.dp else 6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(sticker.accentColor.copy(alpha = 0.16f))
                    .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = sticker.label,
                    fontSize = if (compact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14171E),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
