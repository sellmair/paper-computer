package me.semoro.papercpu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArrowOverlay(
    modifier: Modifier = Modifier.Companion,
    startPosition: Offset,
    endPosition: Offset,
    arrowColor: Color = Color.Companion.Blue,
    arrowWidth: Float = 4f,
    arrowHeadSize: Float = 15f,
    arrowOffestSize: Float = 45f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Create a rectangular spline path
        val path = Path().apply {
            // Start from the left position
            val leftPosition = endPosition
            val rightPosition = startPosition

            // Move to the start position (left)
            moveTo(leftPosition.x, leftPosition.y)
            lineTo(leftPosition.x - arrowOffestSize, leftPosition.y)


            val midY = (leftPosition.y + rightPosition.y) / 2
            lineTo(leftPosition.x - arrowOffestSize, midY)
            // Create a rectangular path with right angles
            // First go horizontally to the middle
            lineTo(rightPosition.x + arrowOffestSize, midY)

            // Then go vertically to the end position's y-coordinate
            lineTo(rightPosition.x + arrowOffestSize, rightPosition.y)
            // Finally go horizontally to the end position (right)
            lineTo(rightPosition.x, rightPosition.y)
        }

        // Draw the path
        drawPath(
            path = path,
            color = arrowColor,
            style = Stroke(width = arrowWidth)
        )

        // Calculate the angle for the arrow head (always pointing from right to left)
        val angle = 2 * PI.toFloat() // 180 degrees, pointing left

        val arrowPath = Path().apply {
            // Move to the right position
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
fun PCOverlay(
    modifier: Modifier = Modifier.Companion,
    rect: Rect,
    boxColor: Color = Color(0xFF6495ED), // Cornflower blue to match the screenshot
    textColor: Color = Color.White,
    opacity: Float = 1f,
    cornerRadius: Float = 20f
) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier.fillMaxSize()) {

        // Measure and draw the "PC" text in white
        val result = measurer.measure("PC")
        val textWidth = result.getLineRight(0) - result.getLineLeft(0)
        val textHeight = result.size.height



        val textBlockRect = Rect(
            rect.topLeft + Offset(- cornerRadius - textWidth, 0f),
            rect.bottomLeft
        )

        val newRect = Rect(
            textBlockRect.topLeft,
            rect.bottomRight
        )


        // Draw the filled rounded rectangle
        drawRoundRect(
            color = boxColor,
            topLeft = newRect.topLeft,
            size = newRect.size,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 10f),
            alpha = opacity
        )
        drawRoundRect(
            boxColor,
            topLeft = textBlockRect.topLeft,
            size = textBlockRect.size,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            alpha = opacity
        )

        // Position the text vertically centered on the left side of the rectangle
        drawText(
            textMeasurer = measurer,
            text = "PC",
            style = TextStyle(color = textColor),
            topLeft = Offset(
                rect.left - cornerRadius / 2 - textWidth,
                rect.top + (rect.height - textHeight) / 2
            )
        )
    }
}


@Composable
@Preview
fun PCOverlayPreview() {
    MaterialTheme {
        Surface {
            PCOverlay(
                rect = Rect(100f, 100f, 300f, 200f)
            )
        }
    }
}

@Composable
@Preview
fun ArrowOverlayPreview() {
    MaterialTheme {
        Surface {
            ArrowOverlay(
                startPosition = Offset(100f, 100f),
                endPosition = Offset(300f, 200f),
                arrowColor = Color.Blue,
                arrowWidth = 5f,
                arrowHeadSize = 20f
            )
        }
    }
}
