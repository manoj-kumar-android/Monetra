package com.monetra.core.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A custom modifier that draws a dashed border around any shape.
 * Perfect for producing a "stitched" thread look for UI designs.
 */
fun Modifier.dashedBorder(
    width: Dp = 1.5.dp,
    color: Color,
    shape: Shape,
    on: Dp = 8.dp,
    off: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(on.toPx(), off.toPx()),
            phase = 0f
        )
    )
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline,
        color = color,
        style = stroke
    )
}
