package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.*

private data class FaqItem(val question: String, val answer: String)

private val faqList = listOf(
    FaqItem(
        question = "How do payments work?",
        answer = "CampusServe doesn't process payments directly. Agree a price in chat, then pay your peer directly (cash or bank transfer) after the service is done."
    ),
    FaqItem(
        question = "How are ratings calculated?",
        answer = "Ratings are calculated from the average star score submitted by verified students after completing a tutoring or service session."
    ),
    FaqItem(
        question = "How do I report a user or listing?",
        answer = "Tap the 'Safety concern? Report a user' link or flag icon to report inappropriate behavior directly to our campus moderation team."
    ),
    FaqItem(
        question = "Editing or pausing my listing",
        answer = "Go to your Profile tab under 'My listings' and tap EDIT to modify title, pricing, or pause your active service."
    )
)

@Composable
fun HelpdeskScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Text(
                text = "Help & Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
        }

        Divider(color = Hairline)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CAMPUSHIVE HELPDESK Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HoneyTint)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "CampusHive HelpDesk",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C6B10),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Need a hand?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Charcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Report a problem and our team will get back to you within 24 hours.",
                        fontSize = 12.sp,
                        color = MutedText,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Opening report problem form...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Report a problem", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // My Reports Ticket Tracker
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("My reports", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
                        Text("1 ticket • under review", fontSize = 12.sp, color = MutedText)
                    }

                    Surface(color = HoneyTint, shape = RoundedCornerShape(10.dp)) {
                        Text("OPEN", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    }
                }
            }

            // Frequently Asked Questions
            Text("FREQUENTLY ASKED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText, letterSpacing = 0.8.sp)

            faqList.forEach { faq ->
                FaqExpandableCard(faq = faq)
            }

            Spacer(Modifier.height(8.dp))

            // Safety Footer Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "Opening safety report form...", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Safety concern? Report a user", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Charcoal, modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun FaqExpandableCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MutedText
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 12.sp,
                        color = MutedText,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
