package com.example.campushive.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class GuideTarget {
    HOME_SEARCH,
    HOME_QUICK_DOCK,
    BOTTOM_CREATE,
    BOTTOM_CHATS,
    BOTTOM_PROFILE
}

data class GuideStep(
    val target: GuideTarget,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val cutoutPadding: Dp = 6.dp,
    val cutoutCornerRadius: Dp = 20.dp
)

private val defaultGuideSteps = listOf(
    GuideStep(
        target = GuideTarget.HOME_SEARCH,
        title = "Find campus services fast",
        description = "Search by subject, skill, or keyword—then filter by category and price to find the right student.",
        icon = Icons.Outlined.Search,
        cutoutPadding = 6.dp,
        cutoutCornerRadius = 20.dp
    ),
    GuideStep(
        target = GuideTarget.HOME_QUICK_DOCK,
        title = "Jump straight in",
        description = "Use the quick dock to post a gig, browse all listings, or open your latest chat in one tap.",
        icon = Icons.Outlined.Explore,
        cutoutPadding = 6.dp,
        cutoutCornerRadius = 24.dp
    ),
    GuideStep(
        target = GuideTarget.BOTTOM_CREATE,
        title = "Offer your own skills",
        description = "Tap or slide onto + anytime to list tutoring, design, tech help, or errands and earn on campus.",
        icon = Icons.Filled.Add,
        cutoutPadding = 8.dp,
        cutoutCornerRadius = 999.dp
    ),
    GuideStep(
        target = GuideTarget.BOTTOM_CHATS,
        title = "Chat, book & review",
        description = "Message providers in real time with photos, GIFs, and stickers—then leave a review after your session.",
        icon = Icons.Outlined.ChatBubbleOutline,
        cutoutPadding = 6.dp,
        cutoutCornerRadius = 28.dp
    ),
    GuideStep(
        target = GuideTarget.BOTTOM_PROFILE,
        title = "Your hub & safety",
        description = "Manage your listings, toggle Dark Mode, reach Helpdesk, or replay this guide whenever you need it.",
        icon = Icons.Outlined.PersonOutline,
        cutoutPadding = 6.dp,
        cutoutCornerRadius = 28.dp
    )
)

@Stable
class AppGuideController {
    private val _targetBounds = mutableStateMapOf<GuideTarget, Rect>()
    val targetBounds: Map<GuideTarget, Rect> get() = _targetBounds

    var isVisible by mutableStateOf(false)
        private set

    var currentStepIndex by mutableIntStateOf(0)
        private set

    var pendingReplay by mutableStateOf(false)

    val steps: List<GuideStep> = defaultGuideSteps

    fun registerTarget(target: GuideTarget, rect: Rect) {
        if (rect.width > 0f && rect.height > 0f && _targetBounds[target] != rect) {
            _targetBounds[target] = rect
        }
    }

    fun start() {
        currentStepIndex = 0
        pendingReplay = false
        isVisible = true
    }

    fun next() {
        if (currentStepIndex < steps.lastIndex) {
            currentStepIndex += 1
        }
    }

    fun previous() {
        if (currentStepIndex > 0) {
            currentStepIndex -= 1
        }
    }

    fun finish() {
        isVisible = false
        pendingReplay = false
    }
}

val LocalAppGuideController = staticCompositionLocalOf<AppGuideController?> { null }

@Composable
fun rememberAppGuideController(): AppGuideController = remember { AppGuideController() }

fun Modifier.guideTarget(target: GuideTarget): Modifier = composed {
    val controller = LocalAppGuideController.current
    if (controller == null) {
        this
    } else {
        this.onGloballyPositioned { coords ->
            if (coords.isAttached) {
                controller.registerTarget(target, coords.boundsInRoot())
            }
        }
    }
}

@Composable
fun AppGuideOverlay(
    controller: AppGuideController,
    onGuideCompleted: () -> Unit
) {
    if (!controller.isVisible) return

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val revealProgress = remember { Animatable(0f) }
    var isDismissing by remember { mutableStateOf(false) }
    var rootSize by remember { mutableStateOf(Size.Zero) }
    var cardHeightPx by remember { mutableIntStateOf(0) }

    val statusBarTopPx = WindowInsets.statusBars.getTop(density).toFloat()
    val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()

    LaunchedEffect(Unit) {
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
        )
    }

    fun dismissAnimated() {
        if (isDismissing) return
        isDismissing = true
        scope.launch {
            revealProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 190, easing = FastOutLinearInEasing)
            )
            controller.finish()
            onGuideCompleted()
        }
    }

    val stepIndex = controller.currentStepIndex.coerceIn(0, controller.steps.lastIndex)
    val step = controller.steps[stepIndex]
    val rawTarget = controller.targetBounds[step.target]

    val paddingPx = with(density) { step.cutoutPadding.toPx() }
    val targetRect = remember(rawTarget, paddingPx, rootSize) {
        if (rawTarget != null && rawTarget.width > 0f && rawTarget.height > 0f) {
            Rect(
                left = (rawTarget.left - paddingPx).coerceAtLeast(0f),
                top = (rawTarget.top - paddingPx).coerceAtLeast(0f),
                right = (rawTarget.right + paddingPx).coerceAtMost(
                    if (rootSize.width > 0f) rootSize.width else rawTarget.right + paddingPx
                ),
                bottom = (rawTarget.bottom + paddingPx).coerceAtMost(
                    if (rootSize.height > 0f) rootSize.height else rawTarget.bottom + paddingPx
                )
            )
        } else {
            val cx = if (rootSize.width > 0f) rootSize.width / 2f else 500f
            val cy = if (rootSize.height > 0f) rootSize.height * 0.35f else 600f
            Rect(cx - 1f, cy - 1f, cx + 1f, cy + 1f)
        }
    }

    val hasValidTarget = rawTarget != null && rawTarget.width > 0f && rawTarget.height > 0f

    val animatedRect by animateRectAsState(
        targetValue = targetRect,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "guideSpotlightRect"
    )

    val desiredCornerPx = with(density) {
        val rawPx = step.cutoutCornerRadius.toPx()
        val maxRadius = (minOf(targetRect.width, targetRect.height) / 2f).coerceAtLeast(1f)
        rawPx.coerceAtMost(maxRadius)
    }
    val animatedCornerPx by animateFloatAsState(
        targetValue = desiredCornerPx,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "guideSpotlightCorner"
    )

    // Subtle pulsing honey halo around the active spotlight
    val pulseTransition = rememberInfiniteTransition(label = "guideHaloPulse")
    val haloAlpha by pulseTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guideHaloAlpha"
    )
    val haloSpreadDp by pulseTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 5.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guideHaloSpread"
    )

    val haloColor = BrandHoney
    val cardGapPx = with(density) { 18.dp.toPx() }
    val safeMarginPx = with(density) { 16.dp.toPx() }

    val targetCardTopPx = remember(targetRect, rootSize, cardHeightPx, statusBarTopPx, navBarBottomPx) {
        if (rootSize.height <= 0f || cardHeightPx <= 0) {
            statusBarTopPx + safeMarginPx * 4f
        } else {
            val isUpperHalf = targetRect.center.y < rootSize.height * 0.52f
            val preferredY = if (isUpperHalf) {
                targetRect.bottom + cardGapPx
            } else {
                targetRect.top - cardHeightPx - cardGapPx
            }
            val minY = statusBarTopPx + safeMarginPx
            val maxY = (rootSize.height - navBarBottomPx - cardHeightPx - safeMarginPx).coerceAtLeast(minY)
            preferredY.coerceIn(minY, maxY)
        }
    }

    val animatedCardTopPx by animateFloatAsState(
        targetValue = targetCardTopPx,
        animationSpec = spring(
            dampingRatio = 0.84f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "guideCardTopY"
    )

    val entranceSlidePx = with(density) { 18.dp.toPx() * (1f - revealProgress.value) }
    val scrimAlpha = 0.70f * revealProgress.value.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { rootSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Swallow background taps during walkthrough
            )
    ) {
        // Spotlight Scrim + Clear Cutout + Breathing Honey Ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(color = Color.Black.copy(alpha = scrimAlpha))

            if (hasValidTarget) {
                val expandOnExit = if (isDismissing) (1f - revealProgress.value) * 32.dp.toPx() else 0f
                val shrinkOnEnter = if (!isDismissing) (1f - revealProgress.value) * 14.dp.toPx() else 0f
                val delta = expandOnExit - shrinkOnEnter

                val drawLeft = animatedRect.left - delta
                val drawTop = animatedRect.top - delta
                val drawWidth = (animatedRect.width + delta * 2f).coerceAtLeast(1f)
                val drawHeight = (animatedRect.height + delta * 2f).coerceAtLeast(1f)
                val radius = animatedCornerPx.coerceAtMost(minOf(drawWidth, drawHeight) / 2f)

                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(drawLeft, drawTop),
                    size = Size(drawWidth, drawHeight),
                    cornerRadius = CornerRadius(radius, radius),
                    blendMode = BlendMode.Clear
                )

                val haloSpreadPx = haloSpreadDp.dp.toPx()
                val ringRadius = (radius + haloSpreadPx).coerceAtMost(minOf(drawWidth, drawHeight) / 2f + haloSpreadPx)
                drawRoundRect(
                    color = haloColor.copy(alpha = haloAlpha * revealProgress.value),
                    topLeft = Offset(drawLeft - haloSpreadPx / 2f, drawTop - haloSpreadPx / 2f),
                    size = Size(drawWidth + haloSpreadPx, drawHeight + haloSpreadPx),
                    cornerRadius = CornerRadius(ringRadius, ringRadius),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Animated Tooltip Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
            contentColor = Charcoal,
            border = BorderStroke(1.dp, Hairline),
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .onSizeChanged { cardHeightPx = it.height }
                .offset { IntOffset(0, (animatedCardTopPx + entranceSlidePx).roundToInt()) }
                .graphicsLayer {
                    alpha = revealProgress.value.coerceIn(0f, 1f)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Step Counter + Skip Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = HoneyTint,
                        shape = Radius.Pill
                    ) {
                        Text(
                            text = "STEP ${stepIndex + 1} OF ${controller.steps.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                            letterSpacing = 0.6.sp
                        )
                    }

                    Text(
                        text = "Skip",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedText,
                        modifier = Modifier
                            .clip(Radius.Pill)
                            .bounceClick(scaleDown = 0.92f) { dismissAnimated() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Directional Step Content Transition
                AnimatedContent(
                    targetState = stepIndex,
                    transitionSpec = {
                        val forward = targetState >= initialState
                        val slideOffset = { fullWidth: Int -> if (forward) fullWidth / 5 else -fullWidth / 5 }
                        (slideInHorizontally(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            initialOffsetX = slideOffset
                        ) + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(180, easing = FastOutLinearInEasing),
                                targetOffsetX = { fullWidth -> if (forward) -fullWidth / 6 else fullWidth / 6 }
                            ) + fadeOut(tween(140)))
                    },
                    label = "guideStepContent"
                ) { index ->
                    val current = controller.steps[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HoneyTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = current.icon,
                                contentDescription = null,
                                tint = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = current.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = current.description,
                                fontSize = 13.sp,
                                color = MutedText,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                HorizontalDivider(thickness = 0.8.dp, color = Hairline)
                Spacer(Modifier.height(14.dp))

                // Progress Pills + Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expanding Pill Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        controller.steps.indices.forEach { i ->
                            val isActive = i == stepIndex
                            val dotWidth by animateDpAsState(
                                targetValue = if (isActive) 22.dp else 6.dp,
                                animationSpec = spring(
                                    dampingRatio = 0.78f,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "guideDotWidth"
                            )
                            Box(
                                modifier = Modifier
                                    .width(dotWidth)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) BrandHoney else Hairline)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = stepIndex > 0,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(120))
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CreamTint,
                                border = BorderStroke(1.dp, Hairline),
                                modifier = Modifier
                                    .size(38.dp)
                                    .bounceClick(scaleDown = 0.90f) { controller.previous() }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous step",
                                        tint = Charcoal,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        val isLast = stepIndex == controller.steps.lastIndex
                        Surface(
                            shape = Radius.Pill,
                            color = Honey,
                            modifier = Modifier
                                .height(38.dp)
                                .bounceClick(scaleDown = 0.94f) {
                                    if (isLast) dismissAnimated() else controller.next()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isLast) "Get Started" else "Next",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnHoneyAccent
                                )
                                Icon(
                                    imageVector = if (isLast) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = OnHoneyAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
