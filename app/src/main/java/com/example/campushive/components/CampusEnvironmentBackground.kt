package com.example.campushive.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

/**
 * Portrait-first modern university campus architectural background illustration for Login & Sign Up.
 * Width-fitted and top-anchored in a 286x432 portrait coordinate space so both campus wings,
 * the elevated skybridge, and the right-of-center butterfly-roof atrium stay fully visible
 * above the 2-option bottom sheet while leaving an uncluttered lower-left courtyard wash
 * for hero typography. Adapts automatically to Light and Dark themes.
 */
@Composable
fun CampusEnvironmentBackground(
    modifier: Modifier = Modifier
) {
    val isDark = CampusThemeState.isDark

    Canvas(modifier = modifier) {
        val viewW = 286f
        val viewH = 432f
        val scale = size.width / viewW

        clipRect {
            // Palette tokens adapted for Light (golden-hour cream) & Dark (architectural dusk)
            val skyTop = if (isDark) Color(0xFF13151B) else Color(0xFFFAF8F2)
            val skyMid = if (isDark) Color(0xFF1A1D26) else Color(0xFFF6EFE0)
            val skyBot = if (isDark) Color(0xFF222633) else Color(0xFFEFE4CE)

            val sunCenter = if (isDark) Color(0xFFFFD87A).copy(alpha = 0.18f) else Color(0xFFFFE299).copy(alpha = 0.72f)
            val sunMid = if (isDark) Color(0xFFFFE6A3).copy(alpha = 0.07f) else Color(0xFFFFF0C4).copy(alpha = 0.28f)

            val cloudColor = if (isDark) Color(0xFF3A4152).copy(alpha = 0.34f) else Color.White.copy(alpha = 0.62f)
            val hillFar = if (isDark) Color(0xFF1E222D).copy(alpha = 0.85f) else Color(0xFFE5DCCB).copy(alpha = 0.72f)
            val hillNear = if (isDark) Color(0xFF242936).copy(alpha = 0.90f) else Color(0xFFDACDBC).copy(alpha = 0.76f)

            val farTowerFront = if (isDark) Color(0xFF282E3B) else Color(0xFFDFD6C7)
            val farTowerSide = if (isDark) Color(0xFF1F2430) else Color(0xFFD0C6B5)
            val farWindow = if (isDark) Color(0xFF4C5A6C).copy(alpha = 0.55f) else Color(0xFFB7C3C9).copy(alpha = 0.55f)

            val concreteLitStart = if (isDark) Color(0xFF343B49) else Color(0xFFF5F0E6)
            val concreteLitEnd = if (isDark) Color(0xFF2A303D) else Color(0xFFE5DCCD)
            val concreteShade = if (isDark) Color(0xFF212631) else Color(0xFFCFC5B6)
            val concreteShadeSoft = if (isDark) Color(0xFF252B37) else Color(0xFFD2C8BA)
            val slabLight = if (isDark) Color(0xFF3C4352) else Color(0xFFF0EBE1)
            val slabMid = if (isDark) Color(0xFF2F3543) else Color(0xFFE5DCCB)
            val roofDark = if (isDark) Color(0xFF11141B) else Color(0xFF262C35)
            val roofCharcoal = if (isDark) Color(0xFF181C25) else Color(0xFF353C47)
            val soffitWarm = if (isDark) Color(0xFF846E48) else Color(0xFFE0C084)

            val glassCoolTop = if (isDark) Color(0xFF3B4F63) else Color(0xFFD5E1E6)
            val glassCoolMid = if (isDark) Color(0xFF2C3C4D) else Color(0xFFB8C8D0)
            val glassCoolBot = if (isDark) Color(0xFF202D3B) else Color(0xFF96AAB5)

            val glassWarmTop = if (isDark) Color(0xFF324354) else Color(0xFFC8D6DD)
            val glassWarmMid = if (isDark) Color(0xFF5D5340) else Color(0xFFE8D8B6)
            val glassWarmBot = if (isDark) Color(0xFFA6874A) else Color(0xFFF6DE9C)

            val interiorGlow = if (isDark) Color(0xFFFFD87A).copy(alpha = 0.36f) else Color(0xFFFFE7A8).copy(alpha = 0.46f)
            val lobbyGlow = if (isDark) Color(0xFFFFD87A).copy(alpha = 0.56f) else Color(0xFFFFEAB5).copy(alpha = 0.80f)
            val reflectionColor = Color.White.copy(alpha = if (isDark) 0.11f else 0.25f)
            val mullionColor = if (isDark) Color(0xFF141920).copy(alpha = 0.72f) else Color(0xFF45505C).copy(alpha = 0.42f)
            val woodFin = if (isDark) Color(0xFF8C7248) else Color(0xFFD4AE6E)
            val woodLouver = if (isDark) Color(0xFF806843) else Color(0xFFCFA96B)
            val pilotisShade = if (isDark) Color(0xFF13171F).copy(alpha = 0.55f) else Color(0xFF68707A).copy(alpha = 0.24f)

            val plazaTop = if (isDark) Color(0xFF212630) else Color(0xFFECE3D1)
            val plazaMid = if (isDark) Color(0xFF1B1F28) else Color(0xFFF3ECE0)
            val plazaBot = if (isDark) Color(0xFF151820) else Color(0xFFF8F5EE)
            val walkwayFill = if (isDark) Color(0xFF2B313D) else Color(0xFFF5F0E5)
            val walkwayLine = if (isDark) Color(0xFF394050) else Color(0xFFDFD5C3)
            val lawnFill = if (isDark) Color(0xFF314333).copy(alpha = 0.55f) else Color(0xFF9EB28E).copy(alpha = 0.48f)

            val treeDark = if (isDark) Color(0xFF2B3D2F) else Color(0xFF7D9472)
            val treeMid = if (isDark) Color(0xFF374E3B) else Color(0xFF8DA381)
            val treeLight = if (isDark) Color(0xFF45604A) else Color(0xFFA1B695)
            val trunkColor = if (isDark) Color(0xFF3D3934) else Color(0xFF7D7265)
            val washBase = if (isDark) Color(0xFF171B24) else Color(0xFFF9F6EF)

            // Seamless full-bleed base below the portrait artwork (behind the white bottom sheet)
            drawRect(color = plazaBot, topLeft = Offset.Zero, size = size)

            withTransform({
                scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
            }) {
                val concreteLitBrush = Brush.horizontalGradient(
                    colors = listOf(concreteLitStart, concreteLitEnd),
                    startX = 0f,
                    endX = viewW
                )
                val glassCoolBrush = Brush.linearGradient(
                    colors = listOf(glassCoolTop, glassCoolMid, glassCoolBot),
                    start = Offset(0f, 160f),
                    end = Offset(90f, 220f)
                )
                val glassWarmBrush = Brush.verticalGradient(
                    colors = listOf(glassWarmTop, glassWarmMid, glassWarmBot),
                    startY = 127f,
                    endY = 246f
                )

                // 1. Golden-Hour Cream Sky & Upper-Right Sun Glow
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to skyTop,
                        0.55f to skyMid,
                        1.0f to skyBot,
                        startY = 0f,
                        endY = 252f
                    ),
                    topLeft = Offset.Zero,
                    size = Size(viewW, 252f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to sunCenter,
                        0.52f to sunMid,
                        1.0f to Color.Transparent,
                        center = Offset(228f, 70f),
                        radius = 115f
                    ),
                    radius = 115f,
                    center = Offset(228f, 70f)
                )

                // Soft Minimal Clouds
                drawPath(
                    path = Path().apply {
                        moveTo(164f, 82f)
                        cubicTo(174f, 75f, 192f, 75f, 202f, 80f)
                        cubicTo(211f, 75f, 226f, 77f, 234f, 84f)
                        cubicTo(240f, 85f, 244f, 89f, 242f, 92f)
                        lineTo(158f, 92f)
                        cubicTo(156f, 88f, 159f, 84f, 164f, 82f)
                        close()
                    },
                    color = cloudColor
                )
                drawPath(
                    path = Path().apply {
                        moveTo(32f, 98f)
                        cubicTo(40f, 93f, 55f, 93f, 64f, 97f)
                        cubicTo(72f, 93f, 84f, 95f, 90f, 101f)
                        lineTo(26f, 101f)
                        cubicTo(27f, 99f, 29f, 98f, 32f, 98f)
                        close()
                    },
                    color = cloudColor.copy(alpha = cloudColor.alpha * 0.78f)
                )

                // 2. Distant Rolling Hills
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 218f)
                        quadraticTo(74f, 196f, 146f, 210f)
                        quadraticTo(218f, 222f, viewW, 202f)
                        lineTo(viewW, 252f)
                        lineTo(0f, 252f)
                        close()
                    },
                    color = hillFar
                )
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 228f)
                        quadraticTo(92f, 212f, 184f, 222f)
                        quadraticTo(242f, 228f, viewW, 216f)
                        lineTo(viewW, 252f)
                        lineTo(0f, 252f)
                        close()
                    },
                    color = hillNear
                )

                // 3. Far Background Academic Tower (Depth Layer)
                drawRect(farTowerFront, Offset(92f, 138f), Size(32f, 106f))
                drawRect(farTowerSide, Offset(124f, 138f), Size(7f, 106f))
                for (wy in listOf(146f, 156f, 166f)) {
                    drawRect(farWindow, Offset(96f, wy), Size(24f, 4f))
                }

                // 4. Left Stepped Innovation Wing
                drawRect(concreteLitBrush, Offset(-4f, 162f), Size(98f, 84f))
                drawRect(concreteShadeSoft, Offset(94f, 162f), Size(10f, 84f))
                drawPolygon(
                    listOf(Offset(-4f, 162f), Offset(96f, 162f), Offset(106f, 166f), Offset(-4f, 166f)),
                    roofCharcoal
                )
                drawRect(slabLight, Offset(-4f, 166f), Size(100f, 3f))

                // Curtain-wall glass bands & reflections
                drawRect(glassCoolBrush, Offset(6f, 174f), Size(80f, 20f))
                drawRect(glassWarmBrush, Offset(6f, 200f), Size(80f, 20f))
                drawPolygon(
                    listOf(Offset(18f, 174f), Offset(32f, 174f), Offset(22f, 194f), Offset(8f, 194f)),
                    reflectionColor
                )
                drawPolygon(
                    listOf(Offset(56f, 200f), Offset(72f, 200f), Offset(62f, 220f), Offset(46f, 220f)),
                    reflectionColor.copy(alpha = reflectionColor.alpha * 0.88f)
                )
                drawRect(slabLight, Offset(4f, 194f), Size(84f, 6f))
                drawRect(slabMid, Offset(4f, 220f), Size(84f, 4f))

                // Warm timber louvers
                for (lx in listOf(8f, 13f, 18f)) {
                    drawRect(woodLouver, Offset(lx, 174f), Size(2.5f, 20f))
                }

                // Shaded ground-level pilotis
                drawRect(pilotisShade, Offset(6f, 224f), Size(80f, 22f))
                for (px in listOf(14f, 44f, 74f)) {
                    drawRect(slabLight, Offset(px, 224f), Size(5f, 22f))
                }

                // 5. Elevated Glass Skybridge
                drawRect(glassCoolBrush, Offset(94f, 180f), Size(50f, 19f))
                drawPolygon(
                    listOf(Offset(104f, 180f), Offset(118f, 180f), Offset(110f, 199f), Offset(96f, 199f)),
                    reflectionColor
                )
                drawRect(roofCharcoal, Offset(94f, 178f), Size(50f, 3f))
                drawRect(slabLight, Offset(94f, 198f), Size(50f, 3.5f))
                drawPath(
                    path = Path().apply {
                        moveTo(98f, 198f)
                        lineTo(108f, 181f)
                        lineTo(118f, 198f)
                        lineTo(128f, 181f)
                        lineTo(138f, 198f)
                    },
                    color = mullionColor,
                    style = Stroke(width = 1f)
                )
                drawRect(concreteShade, Offset(116f, 201.5f), Size(5f, 44.5f))

                // 6. Centerpiece: HELP Subang 2 Modern Glass Atrium (Right-of-Center)
                drawPolygon(
                    listOf(Offset(242f, 126f), Offset(258f, 133f), Offset(258f, 246f), Offset(242f, 246f)),
                    concreteShade
                )
                drawRect(concreteLitBrush, Offset(142f, 124f), Size(100f, 122f))

                // Sweeping cantilevered butterfly roof & warm honey soffit
                drawPolygon(
                    listOf(
                        Offset(132f, 127f),
                        Offset(248f, 112f),
                        Offset(260f, 119f),
                        Offset(260f, 124f),
                        Offset(247f, 118f),
                        Offset(132f, 132f)
                    ),
                    roofDark
                )
                drawPolygon(
                    listOf(Offset(135f, 132f), Offset(247f, 118f), Offset(247f, 124f), Offset(135f, 137f)),
                    soffitWarm
                )

                // Double-height sunlit curtain-wall glass facade
                drawPath(
                    path = Path().apply {
                        moveTo(148f, 138f)
                        lineTo(236f, 127f)
                        lineTo(236f, 246f)
                        lineTo(148f, 246f)
                        close()
                    },
                    brush = glassWarmBrush
                )

                // Interior study lounges & floor plates
                drawRect(soffitWarm.copy(alpha = 0.72f), Offset(148f, 166f), Size(88f, 3f))
                drawRect(soffitWarm.copy(alpha = 0.72f), Offset(148f, 198f), Size(88f, 3f))
                drawRect(interiorGlow, Offset(154f, 148f), Size(28f, 18f))
                drawRect(interiorGlow.copy(alpha = interiorGlow.alpha * 0.87f), Offset(190f, 144f), Size(36f, 22f))
                drawRect(interiorGlow, Offset(158f, 178f), Size(38f, 20f))

                // Diagonal atrium glass reflections
                drawPolygon(
                    listOf(Offset(160f, 136f), Offset(184f, 133f), Offset(158f, 246f), Offset(148f, 246f), Offset(148f, 186f)),
                    reflectionColor
                )
                drawPolygon(
                    listOf(Offset(204f, 131f), Offset(218f, 129f), Offset(194f, 246f), Offset(180f, 246f)),
                    reflectionColor.copy(alpha = reflectionColor.alpha * 0.72f)
                )

                // Crisp architectural mullions
                drawLine(mullionColor, Offset(170f, 135f), Offset(170f, 246f), strokeWidth = 1f)
                drawLine(mullionColor, Offset(192f, 132f), Offset(192f, 246f), strokeWidth = 1f)
                drawLine(mullionColor, Offset(214f, 129f), Offset(214f, 246f), strokeWidth = 1f)
                drawLine(mullionColor.copy(alpha = mullionColor.alpha * 0.9f), Offset(148f, 166f), Offset(236f, 166f), strokeWidth = 1f)
                drawLine(mullionColor.copy(alpha = mullionColor.alpha * 0.9f), Offset(148f, 198f), Offset(236f, 198f), strokeWidth = 1f)

                // Cantilevered entrance canopy & glowing lobby portal
                drawRect(roofDark.copy(alpha = 0.76f), Offset(170f, 222f), Size(46f, 24f))
                drawRect(lobbyGlow, Offset(177f, 226f), Size(32f, 20f))
                drawLine(roofCharcoal, Offset(193f, 226f), Offset(193f, 246f), strokeWidth = 1.2f)
                drawPolygon(
                    listOf(Offset(160f, 218f), Offset(226f, 218f), Offset(230f, 222f), Offset(156f, 222f)),
                    roofDark
                )
                drawRect(slabLight, Offset(160f, 222f), Size(66f, 2f))

                // 7. Right Biophilic Terrace Wing
                drawRect(concreteLitBrush, Offset(256f, 156f), Size(34f, 90f))
                drawRect(glassCoolBrush, Offset(262f, 164f), Size(24f, 20f))
                drawRect(glassWarmBrush, Offset(262f, 192f), Size(24f, 20f))
                drawRect(woodFin, Offset(266f, 162f), Size(2.5f, 52f))
                drawRect(woodFin, Offset(274f, 162f), Size(2.5f, 52f))
                drawPath(
                    path = Path().apply {
                        moveTo(258f, 156f)
                        quadraticTo(266f, 146f, 276f, 151f)
                        quadraticTo(283f, 145f, 290f, 156f)
                        close()
                    },
                    color = treeMid
                )

                // 8. Minimalist Courtyard & Clean Lower Fade (No clutter behind headline)
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to plazaTop,
                        0.55f to plazaMid,
                        1.0f to plazaBot,
                        startY = 246f,
                        endY = viewH
                    ),
                    topLeft = Offset(0f, 246f),
                    size = Size(viewW, viewH - 246f)
                )
                drawRect(walkwayFill, Offset(156f, 246f), Size(74f, 4f))
                drawRect(slabMid, Offset(148f, 250f), Size(90f, 4f))
                drawPolygon(
                    listOf(Offset(154f, 254f), Offset(232f, 254f), Offset(268f, 312f), Offset(118f, 312f)),
                    walkwayFill.copy(alpha = 0.85f)
                )
                drawLine(walkwayLine, Offset(176f, 254f), Offset(152f, 312f), strokeWidth = 1f)
                drawLine(walkwayLine, Offset(193f, 254f), Offset(193f, 312f), strokeWidth = 1f)
                drawLine(walkwayLine, Offset(210f, 254f), Offset(234f, 312f), strokeWidth = 1f)
                drawPolygon(
                    listOf(Offset(0f, 256f), Offset(130f, 256f), Offset(118f, 270f), Offset(0f, 270f)),
                    lawnFill
                )
                drawPolygon(
                    listOf(Offset(248f, 256f), Offset(viewW, 256f), Offset(viewW, 270f), Offset(258f, 270f)),
                    lawnFill
                )

                // 9. Tropical Landscaping Framing Edges Only
                // Left Rain Tree
                drawPolygon(listOf(Offset(24f, 262f), Offset(27f, 226f), Offset(30f, 262f)), trunkColor)
                drawOval(treeMid, Offset(-14f, 194f), Size(56f, 36f))
                drawOval(treeDark, Offset(14f, 201f), Size(44f, 30f))
                drawOval(treeLight, Offset(-2f, 190f), Size(48f, 28f))
                drawPath(
                    path = Path().apply {
                        moveTo(-4f, 266f)
                        quadraticTo(12f, 248f, 30f, 256f)
                        quadraticTo(46f, 246f, 62f, 266f)
                        close()
                    },
                    color = treeDark
                )

                // Right Graceful Palm
                drawPath(
                    path = Path().apply {
                        moveTo(272f, 264f)
                        quadraticTo(268f, 232f, 262f, 204f)
                    },
                    color = trunkColor,
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
                drawPalmFrond(262f, 204f, 246f, 196f, 236f, 205f, 249f, 202f, treeDark)
                drawPalmFrond(262f, 204f, 251f, 188f, 240f, 190f, 252f, 195f, treeMid)
                drawPalmFrond(262f, 204f, 276f, 190f, 288f, 196f, 275f, 198f, treeMid)
                drawPalmFrond(262f, 204f, 264f, 184f, 274f, 182f, 266f, 192f, treeLight)

                // Soft Atmospheric Readability Wash for Lower-Left Headline
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to washBase.copy(alpha = if (isDark) 0.72f else 0.90f),
                        0.62f to washBase.copy(alpha = if (isDark) 0.34f else 0.44f),
                        1.0f to Color.Transparent,
                        center = Offset(92f, 268f),
                        radius = 128f
                    ),
                    radius = 128f,
                    center = Offset(92f, 268f)
                )
            }
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
            quadraticTo(cx2, cy2, x0, y0 + 2f)
            close()
        },
        color = color
    )
}
