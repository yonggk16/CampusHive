package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campushive.ui.theme.CampusThemeState
import com.example.campushive.ui.theme.CardWhite
import com.example.campushive.ui.theme.Charcoal
import com.example.campushive.ui.theme.Cream
import com.example.campushive.ui.theme.DangerButtonBg
import com.example.campushive.ui.theme.Hairline
import com.example.campushive.ui.theme.MutedText
import com.example.campushive.ui.theme.OnDangerButton
import com.example.campushive.ui.theme.Radius
import com.example.campushive.ui.theme.bounceClick

/**
 * Minimal, consistent destructive confirmation card matching CampusHive's card and pill grammar.
 */
@Composable
fun CampusConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cancelLabel: String = "Cancel"
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = Radius.Card,
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shadowElevation = if (CampusThemeState.isDark) 0.dp else 12.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 340.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = Charcoal
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MutedText
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = Radius.Pill,
                        color = Cream,
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .bounceClick(scaleDown = 0.96f, onClick = onDismiss)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cancelLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = Radius.Pill,
                        color = DangerButtonBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .bounceClick(scaleDown = 0.96f, onClick = onConfirm)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnDangerButton,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shared listing deletion dialog used across [EditListingDialog] and [ProfileScreen].
 */
@Composable
fun DeleteListingConfirmDialog(
    listingTitle: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    CampusConfirmDialog(
        title = "Delete listing?",
        message = "Remove \"$listingTitle\" from CampusHive? This can't be undone.",
        confirmLabel = "Delete",
        cancelLabel = "Cancel",
        onConfirm = onConfirmDelete,
        onDismiss = onDismiss
    )
}
