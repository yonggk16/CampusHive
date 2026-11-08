package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.*

/**
 * Design System Section 5:
 * Field — White card, Hairline border (Accent on focus), Eyebrow label, 15/500 value,
 * optional inline hint/validation below. Real input + label semantics.
 */
@Composable
fun CampusField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    hintText: String? = null,
    isHintHighlighted: Boolean = false,
    rightActionText: String? = null,
    onRightActionClick: (() -> Unit)? = null,
    singleLine: Boolean = true,
    minHeight: Dp = 0.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Radius.Card,
            color = SurfaceWhite,
            border = BorderStroke(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) Accent else Hairline
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label.uppercase(),
                        style = CampusTypography.Eyebrow,
                        color = Muted
                    )

                    if (rightActionText != null && onRightActionClick != null) {
                        Box(
                            modifier = Modifier
                                .heightIn(min = 32.dp)
                                .bounceClick(onClick = onRightActionClick),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = rightActionText,
                                style = CampusTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                color = Ink
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Muted.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                            lineHeight = 20.sp
                        ),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(Accent),
                        singleLine = singleLine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier)
                            .onFocusChanged { isFocused = it.isFocused }
                            .semantics { contentDescription = label }
                    )
                }
            }
        }

        if (!hintText.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isHintHighlighted) {
                    Surface(
                        color = HoneyTint,
                        shape = Radius.Tag
                    ) {
                        Text(
                            text = hintText,
                            style = CampusTypography.Caption.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Text(
                        text = hintText,
                        style = CampusTypography.Caption,
                        color = Muted
                    )
                }
            }
        }
    }
}

/**
 * Design System Section 5:
 * Button (primary) — Ink fill, white 15/700 text, pill, min-height 48. Full width on forms.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    isLoading: Boolean = false,
    trailingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = Radius.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = Ink,
            contentColor = SurfaceWhite,
            disabledContainerColor = Ink.copy(alpha = 0.5f),
            disabledContentColor = SurfaceWhite.copy(alpha = 0.8f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .bounceClick(scaleDown = 0.96f)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = SurfaceWhite,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Design System Section 5:
 * Button (small) — Same style, min-height 44, 13px text, auto width (Find, Open, Chat, Edit, Review).
 */
@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Ink,
    contentColor: Color = SurfaceWhite,
    border: BorderStroke? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    Surface(
        shape = Radius.Pill,
        color = containerColor,
        border = border,
        modifier = modifier
            .heightIn(min = 44.dp)
            .bounceClick(scaleDown = 0.96f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Design System Section 5:
 * Chip — Pill, white + Hairline; selected = Ink fill, white text.
 * One component for history, price filters, categories and review tags.
 * 44px minimum touch target.
 */
@Composable
fun CampusChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    Surface(
        shape = Radius.Pill,
        color = if (selected) Ink else SurfaceWhite,
        border = if (selected) null else BorderStroke(1.dp, Hairline),
        modifier = modifier
            .heightIn(min = 44.dp)
            .bounceClick(scaleDown = 0.96f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (selected) SurfaceWhite else Muted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) SurfaceWhite else Ink
            )
        }
    }
}

/**
 * Design System Section 5:
 * Tag — HoneyTint, radius 6, 10px uppercase, weight 700, Ink text.
 */
@Composable
fun CampusTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = HoneyTint,
        shape = Radius.Tag,
        modifier = modifier
    ) {
        Text(
            text = text.uppercase(),
            style = CampusTypography.Tag,
            color = Ink,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

/**
 * Design System Section 5:
 * Verified badge — "✓ Campus Verified" on HoneyTint. Used everywhere.
 */
@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    label: String = "✓ Campus Verified"
) {
    Surface(
        color = HoneyTint,
        shape = Radius.Pill,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Design System Section 5:
 * Circle button — 44 white circle with Hairline: back, close, save, notifications, send.
 * All need aria-label (contentDescription).
 */
@Composable
fun CircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceWhite,
    iconTint: Color = Ink,
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(BorderStroke(1.dp, Hairline), CircleShape)
            .semantics { this.contentDescription = contentDescription }
            .bounceClick(scaleDown = 0.96f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Design System Section 5:
 * AvatarTile — HoneyTint circle for people with initials, optional presence indicator.
 */
@Composable
fun AvatarTile(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    fontSize: TextUnit = 15.sp,
    showOnlineBadge: Boolean = false,
    isOnline: Boolean = false
) {
    val initials = remember(name) {
        name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "S" }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(HoneyTint)
                .border(BorderStroke(1.5.dp, AvatarOutline), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }

        if (showOnlineBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Accent else Muted.copy(alpha = 0.45f))
                    .border(2.dp, SurfaceWhite, CircleShape)
            )
        }
    }
}
