package com.example.campushive.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.campushive.navigation.Screen
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private data class Tab(val screen: Screen, val icon: ImageVector)

private data class ItemBounds(val left: Float = 0f, val width: Float = 0f) {
    val right: Float get() = left + width
    val center: Float get() = left + width / 2f
    fun contains(x: Float): Boolean = width > 0f && x in left..right
}

// All 5 nav slots in visual left-to-right order (index 2 is the center "+" Create action)
private val navSlots = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Create,
    Screen.Chats,
    Screen.Profile,
)

private val homeTab = Tab(Screen.Home, Icons.Outlined.Home)
private val searchTab = Tab(Screen.Search, Icons.Outlined.Search)
private val chatsTab = Tab(Screen.Chats, Icons.Outlined.ChatBubbleOutline)
private val profileTab = Tab(Screen.Profile, Icons.Outlined.PersonOutline)

@Composable
fun BottomBar(
    nav: NavController,
    chatViewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    val conversations by chatViewModel.conversations.collectAsState()
    val totalUnread = remember(conversations) { conversations.sumOf { it.unreadCount } }
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    fun go(s: Screen) {
        if (s == Screen.Create) {
            nav.navigate(s.route) {
                launchSingleTop = true
            }
        } else {
            nav.navigate(s.route) {
                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val selectedSlot = remember(current) {
        when {
            current == null || current == Screen.Home.route -> 0
            current.startsWith("search") || current == Screen.Search.route -> 1
            current.startsWith("chats") || current == Screen.Chats.route -> 3
            current.startsWith("profile") || current == Screen.Profile.route -> 4
            else -> 0
        }
    }

    // Bounds for all 5 slots: 0=Home, 1=Search, 2=Create(+), 3=Chats, 4=Profile
    val slotBounds = remember {
        mutableStateListOf(
            ItemBounds(),
            ItemBounds(),
            ItemBounds(),
            ItemBounds(),
            ItemBounds()
        )
    }
    var isDragging by remember { mutableStateOf(false) }
    var isAddPressed by remember { mutableStateOf(false) }
    var dragPointerX by remember { mutableFloatStateOf(0f) }
    var dragIndicatorLeft by remember { mutableFloatStateOf(0f) }

    fun nearestSlotIndex(pointerX: Float): Int {
        // Prefer exact containment first so hitting "+" or any tab feels crisp
        val directHit = slotBounds.indexOfFirst { it.contains(pointerX) }
        if (directHit != -1) return directHit

        var bestIndex = selectedSlot
        var bestDist = Float.MAX_VALUE
        slotBounds.forEachIndexed { index, bounds ->
            if (bounds.width > 0f) {
                val dist = abs(pointerX - bounds.center)
                if (dist < bestDist) {
                    bestDist = dist
                    bestIndex = index
                }
            }
        }
        return bestIndex
    }

    val activeSlot = if (isDragging) nearestSlotIndex(dragPointerX) else selectedSlot
    val isHoveringAdd = isDragging && activeSlot == 2

    // Haptic tick whenever the finger crosses into any new slot (including "+") while dragging
    var lastHapticSlot by remember { mutableIntStateOf(-1) }
    LaunchedEffect(activeSlot, isDragging) {
        if (isDragging && activeSlot != lastHapticSlot) {
            if (lastHapticSlot != -1) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            lastHapticSlot = activeSlot
        } else if (!isDragging) {
            lastHapticSlot = -1
        }
    }

    // Pill width matches standard tab width (slot 0)
    val pillWidthPx = slotBounds.getOrNull(0)?.width?.takeIf { it > 0f }
        ?: slotBounds.firstOrNull { it.width > 0f }?.width
        ?: 0f
    val selectedBounds = slotBounds.getOrNull(selectedSlot) ?: ItemBounds()

    // Direct 1:1 finger tracking during drag, spring snap on release/tap
    val indicatorLeft = remember { Animatable(0f) }
    var hasInitializedIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(selectedBounds.left, isDragging, pillWidthPx) {
        if (!isDragging && pillWidthPx > 0f) {
            if (!hasInitializedIndicator) {
                indicatorLeft.snapTo(selectedBounds.left)
                hasInitializedIndicator = true
            } else {
                indicatorLeft.animateTo(
                    targetValue = selectedBounds.left,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    val indicatorScale by animateFloatAsState(
        targetValue = when {
            isHoveringAdd -> 0.85f
            isDragging -> 1.04f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navIndicatorScale"
    )

    val addButtonScale by animateFloatAsState(
        targetValue = when {
            isHoveringAdd -> 1.12f
            isAddPressed -> 0.90f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 450f
        ),
        label = "addButtonScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
    ) {
        // Floating Pill Bar — unified tap + drag gesture handler across all 5 slots (including "+")
        Surface(
            shape = Radius.Pill,
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 5.dp)
                    .pointerInput(Unit) {
                        val dragThreshold = (viewConfig.touchSlop * 0.6f).coerceAtLeast(6f)
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downX = down.position.x
                            val startedOnAdd = slotBounds[2].contains(downX)
                            if (startedOnAdd) {
                                isAddPressed = true
                            }

                            var dragging = false
                            var lastX = downX

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()

                                if (change == null || !change.pressed) {
                                    isAddPressed = false
                                    if (dragging) {
                                        val targetSlot = nearestSlotIndex(lastX)
                                        scope.launch { indicatorLeft.snapTo(dragIndicatorLeft) }
                                        isDragging = false
                                        go(navSlots[targetSlot])
                                    } else {
                                        val targetSlot = nearestSlotIndex(downX)
                                        go(navSlots[targetSlot])
                                    }
                                    break
                                }

                                lastX = change.position.x
                                val dx = abs(lastX - downX)

                                if (!dragging && dx > dragThreshold) {
                                    dragging = true
                                    isAddPressed = false
                                    isDragging = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }

                                if (dragging) {
                                    dragPointerX = lastX
                                    val livePillWidth = slotBounds[0].width.takeIf { it > 0f }
                                        ?: slotBounds.firstOrNull { it.width > 0f }?.width
                                        ?: 0f
                                    if (livePillWidth > 0f) {
                                        val liveMinLeft = slotBounds[0].left
                                        val liveMaxLeft = (slotBounds[4].right - livePillWidth).coerceAtLeast(liveMinLeft)
                                        dragIndicatorLeft = (lastX - livePillWidth / 2f).coerceIn(liveMinLeft, liveMaxLeft)
                                    }
                                    change.consume()
                                }
                            }
                        }
                    }
            ) {
                // Sliding / Draggable Selector Indicator Pill (deferred layout-phase read = zero drag recomposition)
                if (pillWidthPx > 0f) {
                    val pillWidthDp = with(density) { pillWidthPx.toDp() }
                    Box(
                        modifier = Modifier
                            .offset {
                                val x = if (isDragging) dragIndicatorLeft else indicatorLeft.value
                                IntOffset(x.roundToInt(), 0)
                            }
                            .width(pillWidthDp)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = indicatorScale
                                scaleY = indicatorScale
                            }
                            .clip(RoundedCornerShape(50))
                            .background(HoneyTint)
                    )
                }

                // Foreground Navigation Items + Inline Center "+" Button
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabItem(
                        tab = homeTab,
                        selected = activeSlot == 0,
                        unreadCount = 0,
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                slotBounds[0] = ItemBounds(
                                    left = coords.positionInParent().x,
                                    width = coords.size.width.toFloat()
                                )
                            }
                    )

                    TabItem(
                        tab = searchTab,
                        selected = activeSlot == 1,
                        unreadCount = 0,
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                slotBounds[1] = ItemBounds(
                                    left = coords.positionInParent().x,
                                    width = coords.size.width.toFloat()
                                )
                            }
                    )

                    // Inline Center "+" Button (slot 2 — supports both tap AND drag-and-drop!)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .onGloballyPositioned { coords ->
                                slotBounds[2] = ItemBounds(
                                    left = coords.positionInParent().x,
                                    width = coords.size.width.toFloat()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Honey,
                            shadowElevation = if (isHoveringAdd) 8.dp else 4.dp,
                            modifier = Modifier
                                .size(46.dp)
                                .graphicsLayer {
                                    scaleX = addButtonScale
                                    scaleY = addButtonScale
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "New listing",
                                    tint = OnHoneyAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    TabItem(
                        tab = chatsTab,
                        selected = activeSlot == 3,
                        unreadCount = totalUnread,
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                slotBounds[3] = ItemBounds(
                                    left = coords.positionInParent().x,
                                    width = coords.size.width.toFloat()
                                )
                            }
                    )

                    TabItem(
                        tab = profileTab,
                        selected = activeSlot == 4,
                        unreadCount = 0,
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                slotBounds[4] = ItemBounds(
                                    left = coords.positionInParent().x,
                                    width = coords.size.width.toFloat()
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: Tab,
    selected: Boolean,
    unreadCount: Int = 0,
    modifier: Modifier
) {
    val activeTabColor = if (CampusThemeState.isDark) Honey else Charcoal
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(50))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.screen.title,
                tint = if (selected) activeTabColor else MutedText,
                modifier = Modifier.size(21.dp)
            )

            if (tab.screen == Screen.Chats && unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .offset(x = 4.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(DangerFgStrong)
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = tab.screen.title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = if (selected) activeTabColor else MutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
