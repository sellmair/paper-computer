package me.semoro.papercpu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArrowOverlay(
    modifier: Modifier = Modifier.Companion,
    startPosition: Offset,
    endPosition: Offset,
    arrowColor: Color = Color.Companion.Blue,
    arrowWidth: Float = 4f,
    arrowHeadSize: Float = 15f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw the arrow line
        drawLine(
            color = arrowColor,
            start = startPosition,
            end = endPosition,
            strokeWidth = arrowWidth
        )

        // Calculate the angle of the line
        val angle = atan2(
            endPosition.y - startPosition.y,
            endPosition.x - startPosition.x
        )

        // Draw the arrow head
        val arrowPath = Path().apply {
            // Move to the end position
            moveTo(endPosition.x, endPosition.y)

            // Draw the first line of the arrow head
            lineTo(
                endPosition.x - arrowHeadSize * cos(angle - PI.toFloat() / 6f),
                endPosition.y - arrowHeadSize * sin(angle - PI.toFloat() / 6f)
            )

            // Draw the second line of the arrow head
            moveTo(endPosition.x, endPosition.y)
            lineTo(
                endPosition.x - arrowHeadSize * cos(angle + PI.toFloat() / 6f),
                endPosition.y - arrowHeadSize * sin(angle + PI.toFloat() / 6f)
            )
        }

        drawPath(
            path = arrowPath,
            color = arrowColor,
            style = Stroke(width = arrowWidth)
        )
    }
}

@Composable
fun BoxOverlay(
    modifier: Modifier = Modifier.Companion,
    rect: Rect,
    boxColor: Color = Color.Companion.Blue,
    boxStrokeWidth: Float = 4f
) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier.fillMaxSize()) {

        val result = measurer.measure("PC")
        drawText(result, topLeft = rect.topLeft - Offset(result.getLineRight(0) - result.getLineLeft(0), 0f))


        // Draw the box around the rectangle
        drawRect(
            color = boxColor,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(width = boxStrokeWidth)
        )

    }
}
