package com.example.campushive.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.campushive.ui.theme.CampusThemeState
import com.example.campushive.ui.theme.Cream
import kotlin.math.max

/**
 * Modern university campus architectural background illustration for Login & Sign Up heroes.
 * Scales crisply across aspect ratios (bottom-anchored so the skyline sits just above the
 * auth bottom sheet while keeping the upper-left sky airy for hero typography) and adapts
 * automatically to Light and Dark themes.
 */
@Composable
fun CampusEnvironmentBackground(
    modifier: Modifier = Modifier
) {
    val isDark = CampusThemeState.isDark
    val baseSurface = Cream

    Canvas(modifier = modifier) {
        val viewW = 680f
        val viewH = 340f

        val scale = max(size.width / viewW, size.height / viewH)
        val dx = (size.width - viewW * scale) * 0.5f
        val dy = size.height - viewH * scale

        clipRect {
            // Palette tokens adapted for Light (golden-hour cream) & Dark (architectural dusk)
            val skyTop = if (isDark) Color(0xFF14151A) else Color(0xFFFAF8F2)
            val skyMid = if (isDark) Color(0xFF1A1C24) else Color(0xFFF6EFE0)
            val skyBot = if (isDark) Color(0xFF222530) else Color(0xFFEFE4CE)

            val sunCenter = if (isDark) Color(0xFFFFD87A).copy(alpha = 0.16f) else Color(0xFFFFE6A3).copy(alpha = 0.72f)
            val sunMid = if (isDark) Color(0xFFFFE6A3).copy(alpha = 0.06f) else Color(0xFFFFF1C9).copy(alpha = 0.30f)

            val cloudColor = if (isDark) Color(0xFF3A4150).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.62f)
            val hillFar = if (isDark) Color(0xFF1E222B).copy(alpha = 0.85f) else Color(0xFFE3DACB).copy(alpha = 0.72f)
            val hillNear = if (isDark) Color(0xFF232833).copy(alpha = 0.90f) else Color(0xFFD9CEBC).copy(alpha = 0.76f)

            val farTowerFront = if (isDark) Color(0xFF282D38) else Color(0xFFDED5C6)
            val farTowerSide = if (isDark) Color(0xFF1F242E) else Color(0xFFCFC5B4)
            val farWindow = if (isDark) Color(0xFF4A5868).copy(alpha = 0.55f) else Color(0xFFB8C4CA).copy(alpha = 0.55f)

            val concreteLitStart = if (isDark) Color(0xFF333946) else Color(0xFFF4EFE4)
            val concreteLitEnd = if (isDark) Color(0xFF2A303B) else Color(0xFFE5DDD0)
            val concreteShadeStart = if (isDark) Color(0xFF232832) else Color(0xFFD4CBBF)
            val concreteShadeEnd = if (isDark) Color(0xFF1B2029) else Color(0xFFC2B8AA)
            val slabLight = if (isDark) Color(0xFF3A404E) else Color(0xFFEFEAE0)
            val slabMid = if (isDark) Color(0xFF2F3542) else Color(0xFFE6DEC9)
            val roofDark = if (isDark) Color(0xFF13161C) else Color(0xFF2F3640)
            val roofCharcoal = if (isDark) Color(0xFF191D26) else Color(0xFF3B424D)
            val soffitWarm = if (isDark) Color(0xFF7D6946) else Color(0xFFDFC38D)

            val glassTop = if (isDark) Color(0xFF3A4E61) else Color(0xFFD7E1E6)
            val glassMid = if (isDark) Color(0xFF2B3B4B) else Color(0xFFB9C8D0)
            val glassBot = if (isDark) Color(0xFF202D3A) else Color(0xFF8FA3AE)

            val glassWarmTop = if (isDark) Color(0xFF324354) else Color(0xFFC8D6DD)
            val glassWarmMid = if (isDark) Color(0xFF5A5240) else Color(0xFFE7D8B7)
            val glassWarmBot = if (isDark) Color(0xFF9E8248) else Color(0xFFF6DF9F)

            val interiorGlow = if (isDark) Color(0xFFFFD87A).copy(alpha = 0.34f) else Color(0xFFFFE8AD).copy(alpha = 0.48f)
            val reflectionColor = Color.White.copy(alpha = if (isDark) 0.11f else 0.26f)
            val mullionColor = if (isDark) Color(0xFF141920).copy(alpha = 0.72f) else Color(0xFF45505C).copy(alpha = 0.48f)
            val woodFin = if (isDark) Color(0xFF8C7248) else Color(0xFFD4AE6E)
            val woodLouver = if (isDark) Color(0xFF7F6844) else Color(0xFFC9A875)

            val plazaTop = if (isDark) Color(0xFF232730) else Color(0xFFE8DFCE)
            val plazaBot = if (isDark) Color(0xFF1B1F27) else Color(0xFFDDD2BE)
            val walkwayFill = if (isDark) Color(0xFF2D323E) else Color(0xFFF3EDE1)
            val walkwayLine = if (isDark) Color(0xFF3B4252) else Color(0xFFDDD3C0)
            val lawnFill = if (isDark) Color(0xFF334434).copy(alpha = 0.65f) else Color(0xFF9EB28E).copy(alpha = 0.62f)

            val treeDark = if (isDark) Color(0xFF2B3C2E) else Color(0xFF7D9472)
            val treeMid = if (isDark) Color(0xFF374C3A) else Color(0xFF8DA381)
            val treeLight = if (isDark) Color(0xFF445D47) else Color(0xFFA1B695)
            val trunkColor = if (isDark) Color(0xFF3D3934) else Color(0xFF7D7265)
            val silhouetteColor = if (isDark) Color(0xFF9BA4B5).copy(alpha = 0.45f) else Color(0xFF4A525D).copy(alpha = 0.64f)

            withTransform({
                translate(left = dx, top = dy)
                scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
            }) {
                val concreteLitBrush = Brush.horizontalGradient(
                    colors = listOf(concreteLitStart, concreteLitEnd),
                    startX = 24f,
                    endX = 654f
                )
                val concreteShadeBrush = Brush.horizontalGradient(
                    colors = listOf(concreteShadeStart, concreteShadeEnd),
                    startX = 198f,
                    endX = 490f
                )
                val glassMainBrush = Brush.linearGradient(
                    colors = listOf(glassTop, glassMid, glassBot),
                    start = Offset(0f, 150f),
                    end = Offset(120f, 240f)
                )
                val glassWarmBrush = Brush.verticalGradient(
                    colors = listOf(glassWarmTop, glassWarmMid, glassWarmBot),
                    startY = 118f,
                    endY = 262f
                )

                // 1. Sky & Upper-Right Sun Glow
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to skyTop,
                        0.58f to skyMid,
                        1.0f to skyBot,
                        startY = 0f,
                        endY = viewH
                    ),
                    topLeft = Offset.Zero,
                    size = Size(viewW, viewH)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to sunCenter,
                        0.48f to sunMid,
                        1.0f to Color.Transparent,
                        center = Offset(530f, 62f),
                        radius = 230f
                    ),
                    radius = 230f,
                    center = Offset(530f, 62f)
                )

                // Subtle Clouds
                drawPath(
                    path = Path().apply {
                        moveTo(410f, 52f)
                        cubicTo(425f, 44f, 448f, 44f, 462f, 50f)
                        cubicTo(474f, 44f, 494f, 46f, 504f, 54f)
                        cubicTo(514f, 55f, 520f, 60f, 518f, 64f)
                        lineTo(402f, 64f)
                        cubicTo(400f, 59f, 404f, 54f, 410f, 52f)
                        close()
                    },
                    color = cloudColor
                )
                drawPath(
                    path = Path().apply {
                        moveTo(556f, 76f)
                        cubicTo(566f, 71f, 584f, 71f, 595f, 75f)
                        cubicTo(605f, 71f, 620f, 73f, 628f, 79f)
                        lineTo(548f, 79f)
                        cubicTo(549f, 77f, 552f, 76f, 556f, 76f)
                        close()
                    },
                    color = cloudColor.copy(alpha = cloudColor.alpha * 0.8f)
                )

                // 2. Distant Hills
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 232f)
                        quadraticTo(110f, 198f, 235f, 218f)
                        quadraticTo(360f, 236f, 485f, 210f)
                        quadraticTo(590f, 196f, 680f, 214f)
                        lineTo(680f, 260f)
                        lineTo(0f, 260f)
                        close()
                    },
                    color = hillFar
                )
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 242f)
                        quadraticTo(160f, 218f, 330f, 230f)
                        quadraticTo(505f, 242f, 680f, 224f)
                        lineTo(680f, 265f)
                        lineTo(0f, 265f)
                        close()
                    },
                    color = hillNear
                )

                // 3. Far Background Academic Tower (Depth Layer)
                drawRect(farTowerFront, Offset(218f, 132f), Size(66f, 128f))
                drawRect(farTowerSide, Offset(284f, 132f), Size(14f, 128f))
                for (wy in listOf(142f, 156f, 170f, 184f)) {
                    drawRect(farWindow, Offset(224f, wy), Size(54f, 6f))
                }

                // 4. LEFT WING: Modern Innovation & Lecture Block
                drawRect(concreteLitBrush, Offset(24f, 152f), Size(174f, 110f))
                drawRect(concreteShadeBrush, Offset(198f, 152f), Size(18f, 110f))
                drawPolygon(
                    listOf(Offset(14f, 152f), Offset(204f, 152f), Offset(222f, 158f), Offset(14f, 158f)),
                    roofCharcoal
                )
                drawRect(slabLight, Offset(14f, 158f), Size(190f, 4f))

                // Curtain-wall glass bands
                drawRect(glassMainBrush, Offset(34f, 168f), Size(154f, 28f))
                drawRect(glassWarmBrush, Offset(34f, 204f), Size(154f, 28f))

                // Diagonal glass reflections
                drawPolygon(listOf(Offset(48f, 168f), Offset(68f, 168f), Offset(54f, 196f), Offset(36f, 196f)), reflectionColor)
                drawPolygon(listOf(Offset(118f, 168f), Offset(144f, 168f), Offset(130f, 196f), Offset(104f, 196f)), reflectionColor.copy(alpha = reflectionColor.alpha * 0.8f))
                drawPolygon(listOf(Offset(62f, 204f), Offset(82f, 204f), Offset(68f, 232f), Offset(48f, 232f)), reflectionColor.copy(alpha = reflectionColor.alpha * 0.9f))
                drawPolygon(listOf(Offset(142f, 204f), Offset(164f, 204f), Offset(150f, 232f), Offset(128f, 232f)), reflectionColor.copy(alpha = reflectionColor.alpha * 0.75f))

                // Slab dividers & mullions
                drawRect(slabLight, Offset(30f, 196f), Size(166f, 8f))
                drawRect(slabMid, Offset(30f, 232f), Size(166f, 6f))
                for (mx in listOf(65f, 96f, 127f, 158f)) {
                    drawLine(mullionColor, Offset(mx, 168f), Offset(mx, 232f), strokeWidth = 1f)
                }

                // Warm timber louvers
                for (lx in listOf(34f, 41f, 48f, 55f)) {
                    drawRect(woodLouver, Offset(lx, 168f), Size(3f, 28f))
                }

                // Shaded ground-level pilotis
                drawRect(Color(0xFF6D747E).copy(alpha = 0.32f), Offset(34f, 238f), Size(154f, 24f))
                for (px in listOf(42f, 86f, 130f, 174f)) {
                    drawRect(slabLight, Offset(px, 238f), Size(7f, 24f))
                }

                // 5. ELEVATED GLASS SKYBRIDGE
                drawRect(glassMainBrush, Offset(198f, 184f), Size(102f, 28f))
                drawPolygon(listOf(Offset(212f, 184f), Offset(234f, 184f), Offset(222f, 212f), Offset(200f, 212f)), reflectionColor)
                drawPolygon(listOf(Offset(262f, 184f), Offset(276f, 184f), Offset(264f, 212f), Offset(250f, 212f)), reflectionColor.copy(alpha = reflectionColor.alpha * 0.75f))
                drawRect(roofCharcoal, Offset(198f, 182f), Size(102f, 3.5f))
                drawRect(slabLight, Offset(198f, 210f), Size(102f, 4.5f))
                drawRect(concreteShadeEnd, Offset(198f, 214.5f), Size(102f, 2.5f))
                drawPath(
                    path = Path().apply {
                        moveTo(206f, 210f)
                        lineTo(222f, 185f)
                        lineTo(238f, 210f)
                        lineTo(254f, 185f)
                        lineTo(270f, 210f)
                        lineTo(286f, 185f)
                        lineTo(298f, 206f)
                    },
                    color = mullionColor,
                    style = Stroke(width = 1.2f)
                )
                drawRect(concreteShadeStart, Offset(245f, 217f), Size(8f, 45f))
                drawRect(concreteShadeEnd, Offset(253f, 217f), Size(3f, 45f))

                // 6. CENTERPIECE: Modern Glass Atrium & Library Hub
                drawPolygon(
                    listOf(Offset(462f, 116f), Offset(490f, 126f), Offset(490f, 262f), Offset(462f, 262f)),
                    concreteShadeStart
                )
                drawRect(concreteLitBrush, Offset(296f, 114f), Size(166f, 148f))

                // Angled cantilevered butterfly roof overhang
                drawPolygon(
                    listOf(
                        Offset(282f, 118f),
                        Offset(476f, 98f),
                        Offset(494f, 108f),
                        Offset(494f, 114f),
                        Offset(474f, 106f),
                        Offset(282f, 124f)
                    ),
                    roofDark
                )
                drawPolygon(
                    listOf(Offset(286f, 124f), Offset(474f, 106f), Offset(474f, 113f), Offset(286f, 130f)),
                    soffitWarm
                )

                // Double-height curtain-wall glass facade
                drawPath(
                    path = Path().apply {
                        moveTo(306f, 132f)
                        lineTo(454f, 118f)
                        lineTo(454f, 262f)
                        lineTo(306f, 262f)
                        close()
                    },
                    brush = glassWarmBrush
                )

                // Warm interior floor plates & glowing study lounges
                drawRect(soffitWarm.copy(alpha = 0.72f), Offset(306f, 164f), Size(148f, 4f))
                drawRect(soffitWarm.copy(alpha = 0.72f), Offset(306f, 202f), Size(148f, 4f))
                drawRect(interiorGlow, Offset(316f, 144f), Size(44f, 20f))
                drawRect(interiorGlow.copy(alpha = interiorGlow.alpha * 0.85f), Offset(378f, 138f), Size(56f, 26f))
                drawRect(interiorGlow, Offset(322f, 178f), Size(60f, 24f))
                drawRect(interiorGlow.copy(alpha = interiorGlow.alpha * 0.8f), Offset(404f, 178f), Size(40f, 24f))

                // Diagonal atrium glass reflections
                drawPolygon(
                    listOf(Offset(326f, 130f), Offset(364f, 126f), Offset(322f, 262f), Offset(306f, 262f), Offset(306f, 192f)),
                    reflectionColor
                )
                drawPolygon(
                    listOf(Offset(402f, 123f), Offset(424f, 121f), Offset(384f, 262f), Offset(362f, 262f)),
                    reflectionColor.copy(alpha = reflectionColor.alpha * 0.72f)
                )

                // Atrium vertical & horizontal mullions
                drawLine(mullionColor, Offset(336f, 129f), Offset(336f, 262f), strokeWidth = 1.2f)
                drawLine(mullionColor, Offset(366f, 126f), Offset(366f, 262f), strokeWidth = 1.2f)
                drawLine(mullionColor, Offset(396f, 123f), Offset(396f, 262f), strokeWidth = 1.2f)
                drawLine(mullionColor, Offset(426f, 120f), Offset(426f, 262f), strokeWidth = 1.2f)
                for (hy in listOf(164f, 202f, 234f)) {
                    drawLine(mullionColor, Offset(306f, hy), Offset(454f, hy), strokeWidth = 1.2f)
                }

                // Cantilevered entrance canopy & glowing lobby portal
                drawRect(roofDark.copy(alpha = 0.76f), Offset(342f, 232f), Size(76f, 30f))
                drawRect(Color(0xFFFFEAB5).copy(alpha = if (isDark) 0.55f else 0.78f), Offset(352f, 236f), Size(56f, 26f))
                drawLine(roofCharcoal, Offset(380f, 236f), Offset(380f, 262f), strokeWidth = 1.5f)
                drawPolygon(listOf(Offset(326f, 227f), Offset(434f, 227f), Offset(440f, 232f), Offset(320f, 232f)), roofDark)
                drawRect(slabLight, Offset(326f, 232f), Size(108f, 2.5f))

                // 7. RIGHT WING: Terraced Biophilic Student Hub
                drawRect(concreteLitBrush, Offset(498f, 148f), Size(156f, 114f))
                drawRect(concreteShadeStart, Offset(490f, 148f), Size(8f, 114f))
                drawRect(slabLight, Offset(526f, 128f), Size(128f, 20f))
                drawRect(roofCharcoal, Offset(522f, 125f), Size(136f, 4f))

                // Terrace planters
                drawPath(
                    path = Path().apply {
                        moveTo(504f, 148f)
                        quadraticTo(512f, 138f, 522f, 144f)
                        quadraticTo(530f, 137f, 538f, 148f)
                        close()
                    },
                    color = treeMid
                )
                drawPath(
                    path = Path().apply {
                        moveTo(538f, 125f)
                        quadraticTo(548f, 115f, 560f, 121f)
                        quadraticTo(570f, 114f, 580f, 125f)
                        close()
                    },
                    color = treeLight
                )

                // Glass balconies & warm vertical fins
                drawRect(glassMainBrush, Offset(508f, 158f), Size(136f, 26f))
                drawRect(glassWarmBrush, Offset(508f, 194f), Size(136f, 28f))
                drawRect(slabLight, Offset(504f, 184f), Size(144f, 10f))
                drawRect(slabMid, Offset(504f, 222f), Size(144f, 8f))
                for (fx in listOf(518f, 530f, 542f, 554f, 596f, 608f, 620f, 632f)) {
                    drawRect(woodFin, Offset(fx, 156f), Size(3.5f, 68f))
                }

                // Ground-level café frontage
                drawRect(Color(0xFF5A6370).copy(alpha = 0.30f), Offset(508f, 230f), Size(136f, 32f))
                drawRect(Color(0xFFFFE6A8).copy(alpha = if (isDark) 0.42f else 0.56f), Offset(518f, 234f), Size(44f, 28f))
                drawRect(glassTop.copy(alpha = 0.65f), Offset(572f, 234f), Size(58f, 28f))

                // 8. CAMPUS PLAZA, STEPS & PERSPECTIVE WALKWAY
                drawRect(
                    brush = Brush.verticalGradient(listOf(plazaTop, plazaBot), startY = 262f, endY = 340f),
                    topLeft = Offset(0f, 262f),
                    size = Size(viewW, 78f)
                )
                drawRect(walkwayFill, Offset(320f, 262f), Size(120f, 5f))
                drawRect(slabMid, Offset(310f, 267f), Size(140f, 5f))
                drawRect(concreteShadeStart, Offset(300f, 272f), Size(160f, 5f))

                // Perspective walkway
                drawPolygon(
                    listOf(Offset(318f, 277f), Offset(442f, 277f), Offset(528f, 340f), Offset(232f, 340f)),
                    walkwayFill
                )
                drawLine(walkwayLine, Offset(348f, 277f), Offset(306f, 340f), strokeWidth = 1.2f)
                drawLine(walkwayLine, Offset(380f, 277f), Offset(380f, 340f), strokeWidth = 1.2f)
                drawLine(walkwayLine, Offset(412f, 277f), Offset(454f, 340f), strokeWidth = 1.2f)
                drawLine(walkwayLine, Offset(286f, 298f), Offset(474f, 298f), strokeWidth = 1f)
                drawLine(walkwayLine, Offset(258f, 319f), Offset(502f, 319f), strokeWidth = 1f)

                // Lawn strips & planter benches
                drawPolygon(listOf(Offset(0f, 278f), Offset(266f, 278f), Offset(246f, 296f), Offset(0f, 296f)), lawnFill)
                drawPolygon(listOf(Offset(488f, 278f), Offset(680f, 278f), Offset(680f, 296f), Offset(508f, 296f)), lawnFill)
                drawRoundRect(walkwayFill, Offset(128f, 276f), Size(74f, 7f), CornerRadius(2f, 2f))
                drawRect(concreteShadeEnd, Offset(128f, 283f), Size(74f, 3f))
                drawRoundRect(walkwayFill, Offset(518f, 276f), Size(68f, 7f), CornerRadius(2f, 2f))
                drawRect(concreteShadeEnd, Offset(518f, 283f), Size(68f, 3f))

                // Minimalist Campus Lamp Posts
                for (lx in listOf(276f, 484f)) {
                    drawLine(roofCharcoal, Offset(lx, 226f), Offset(lx, 278f), strokeWidth = 2f)
                    drawRoundRect(roofCharcoal, Offset(lx - 5f, 224f), Size(12f, 3f), CornerRadius(1.5f, 1.5f))
                    drawCircle(
                        color = Color(0xFFFFE6A3).copy(alpha = if (isDark) 0.55f else 0.65f),
                        radius = if (isDark) 8f else 5f,
                        center = Offset(lx + 1f, 228f)
                    )
                }

                // Subtle Student Silhouettes
                drawStudentSilhouette(352f, 264f, 1.0f, silhouetteColor)
                drawStudentSilhouette(364f, 266f, 0.92f, silhouetteColor.copy(alpha = silhouetteColor.alpha * 0.88f))
                drawStudentSilhouette(432f, 268f, 0.92f, silhouetteColor.copy(alpha = silhouetteColor.alpha * 0.82f))

                // 9. LUSH MODERN LANDSCAPING (Framing Edges)
                // Left Rain Tree
                drawPolygon(listOf(Offset(52f, 282f), Offset(56f, 236f), Offset(61f, 282f)), trunkColor)
                drawLine(trunkColor, Offset(57f, 254f), Offset(42f, 238f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                drawLine(trunkColor, Offset(58f, 250f), Offset(74f, 236f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                drawOval(treeMid, Offset(8f, 202f), Size(68f, 40f))
                drawOval(treeDark, Offset(46f, 208f), Size(56f, 36f))
                drawOval(treeLight, Offset(26f, 194f), Size(60f, 36f))

                // Mid-left Ornamental Tree
                drawRect(trunkColor, Offset(218f, 246f), Size(4f, 24f))
                drawOval(treeMid, Offset(202f, 224f), Size(36f, 28f))
                drawOval(treeLight, Offset(203f, 224f), Size(24f, 18f))

                // Right Palm Accent
                drawPath(
                    path = Path().apply {
                        moveTo(636f, 286f)
                        quadraticTo(630f, 242f, 622f, 204f)
                    },
                    color = trunkColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
                drawPalmFrond(622f, 204f, 600f, 194f, 584f, 206f, 604f, 202f, treeDark)
                drawPalmFrond(622f, 204f, 608f, 182f, 592f, 184f, 608f, 190f, treeMid)
                drawPalmFrond(622f, 204f, 642f, 184f, 662f, 192f, 642f, 194f, treeMid)
                drawPalmFrond(622f, 204f, 648f, 202f, 666f, 218f, 644f, 210f, treeDark)
                drawPalmFrond(622f, 204f, 624f, 176f, 638f, 172f, 628f, 186f, treeLight)

                // Low Foreground Shrubs
                drawPath(
                    path = Path().apply {
                        moveTo(-8f, 292f)
                        quadraticTo(16f, 266f, 46f, 278f)
                        quadraticTo(72f, 264f, 98f, 282f)
                        quadraticTo(112f, 276f, 124f, 292f)
                        close()
                    },
                    color = treeDark
                )
                drawPath(
                    path = Path().apply {
                        moveTo(578f, 292f)
                        quadraticTo(598f, 272f, 626f, 280f)
                        quadraticTo(652f, 264f, 688f, 284f)
                        lineTo(688f, 296f)
                        lineTo(578f, 296f)
                        close()
                    },
                    color = treeDark
                )
            }

            // Soft upper-left readability wash so hero title stays crisp over the sky
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseSurface.copy(alpha = if (isDark) 0.55f else 0.72f),
                        baseSurface.copy(alpha = if (isDark) 0.22f else 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.22f, size.height * 0.42f),
                    radius = size.width * 0.65f
                ),
                topLeft = Offset.Zero,
                size = size
            )

            // Smooth bottom blend into the canvas right behind the 28dp rounded bottom sheet
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, baseSurface.copy(alpha = 0.85f)),
                    startY = size.height * 0.76f,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height * 0.76f),
                size = Size(size.width, size.height * 0.24f)
            )
        }
    }
}

private fun DrawScope.drawPolygon(points: List<Offset>, color: Color) {
    if (points.size < 3) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawStudentSilhouette(
    cx: Float,
    headY: Float,
    scale: Float,
    color: Color
) {
    drawCircle(color = color, radius = 3f * scale, center = Offset(cx, headY))
    val topY = headY + 4f * scale
    val botY = headY + 16f * scale
    val midY = headY + 10f * scale
    drawPath(
        path = Path().apply {
            moveTo(cx - 3f * scale, topY)
            quadraticTo(cx, topY - 1f * scale, cx + 3f * scale, topY)
            lineTo(cx + 4f * scale, botY)
            lineTo(cx + 1f * scale, botY)
            lineTo(cx, midY)
            lineTo(cx - 1f * scale, botY)
            lineTo(cx - 4f * scale, botY)
            close()
        },
        color = color
    )
}

private fun DrawScope.drawPalmFrond(
    x0: Float, y0: Float,
    cx1: Float, cy1: Float,
    x1: Float, y1: Float,
    cx2: Float, cy2: Float,
    color: Color
) {
    drawPath(
        path = Path().apply {
            moveTo(x0, y0)
            quadraticTo(cx1, cy1, x1, y1)
            quadraticTo(cx2, cy2, x0, y0 + 2.5f)
            close()
        },
        color = color
    )
}
