package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import com.example.campushive.ui.theme.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*

@Suppress("unused")
@Composable
fun ListingCard(
    listing: Listing,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(Radius.Inner)
            .bounceClick(onClick = onClick),
        shape = Radius.Inner,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Hairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServiceCategoryIconBox(
                    category = listing.category,
                    size = 46.dp,
                    shape = Radius.Inner,
                    symbolFontSize = 18.sp,
                    iconSize = 22.dp
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = HoneyTint,
                            shape = Radius.Tag
                        ) {
                            Text(
                                text = listing.category.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = if (listing.reviewCount > 0) StarGold else MutedText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = if (listing.reviewCount > 0) "%.1f (${listing.reviewCount})".format(listing.rating) else "New",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = listing.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Provider Name & Verification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = listing.providerName,
                        fontSize = 12.sp,
                        color = Charcoal,
                        fontWeight = FontWeight.Bold
                    )
                    if (listing.isVerifiedStudent) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified Student",
                            tint = VerifiedGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = Hairline, thickness = 0.8.dp)

            Spacer(Modifier.height(8.dp))

            // Campus Location & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listing.campusLocation,
                    fontSize = 11.sp,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "RM${"%.0f".format(listing.price)} ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Text(
                        text = listing.pricingType.shortLabel,
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }
        }
    }
}
