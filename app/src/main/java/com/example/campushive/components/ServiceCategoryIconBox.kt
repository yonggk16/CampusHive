package com.example.campushive.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.model.getCategoryVisual
import com.example.campushive.ui.theme.Charcoal

@Composable
fun ServiceCategoryIconBox(
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    shape: Shape = RoundedCornerShape(14.dp),
    symbolFontSize: TextUnit = 18.sp,
    iconSize: Dp = 20.dp
) {
    val visual = getCategoryVisual(category)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(visual.bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (visual.symbol != null) {
            Text(
                text = visual.symbol,
                fontSize = symbolFontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Charcoal
            )
        } else {
            Icon(
                imageVector = visual.icon,
                contentDescription = category,
                tint = Charcoal,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
