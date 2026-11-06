package com.example.edgepulse.model

import kotlin.math.abs

/**
 * Geometric calculations for the square perimeter.
 * The perimeter is parameterized as a normalized value p in [0.0, 1.0).
 *
 * Edge mappings:
 * - [0.00, 0.25): Top edge, from (0, 0) to (1, 0)
 * - [0.25, 0.50): Right edge, from (1, 0) to (1, 1)
 * - [0.50, 0.75): Bottom edge, from (1, 1) to (0, 1)
 * - [0.75, 1.00): Left edge, from (0, 1) to (0, 0)
 */
object PerimeterGeometry {

    data class PointF(val x: Float, val y: Float)

    /**
     * Normalizes any float to [0.0, 1.0).
     */
    fun normalize(p: Float): Float {
        val rem = p % 1.0f
        return if (rem < 0f) rem + 1.0f else rem
    }

    /**
     * Converts normalized perimeter position p to normalized 2D point (x, y) in [0.0, 1.0] x [0.0, 1.0].
     */
    fun perimeterToPoint(p: Float): PointF {
        val normP = normalize(p)
        return when {
            normP < 0.25f -> {
                // Top edge: (0,0) -> (1,0)
                PointF(normP * 4.0f, 0.0f)
            }
            normP < 0.50f -> {
                // Right edge: (1,0) -> (1,1)
                PointF(1.0f, (normP - 0.25f) * 4.0f)
            }
            normP < 0.75f -> {
                // Bottom edge: (1,1) -> (0,1)
                PointF(1.0f - (normP - 0.50f) * 4.0f, 1.0f)
            }
            else -> {
                // Left edge: (0,1) -> (0,0)
                PointF(0.0f, 1.0f - (normP - 0.75f) * 4.0f)
            }
        }
    }

    /**
     * Computes the shortest distance between two perimeter points along the perimeter in [0.0, 0.5].
     */
    fun shortestDistance(p1: Float, p2: Float): Float {
        val norm1 = normalize(p1)
        val norm2 = normalize(p2)
        val diff = abs(norm1 - norm2)
        return minOf(diff, 1.0f - diff)
    }

    /**
     * Checks if a point p falls within an angular segment on the perimeter [start, end].
     * Handles wrap-around through 0.0/1.0.
     */
    fun isPointInSegment(p: Float, start: Float, end: Float): Boolean {
        val normP = normalize(p)
        val normStart = normalize(start)
        val normEnd = normalize(end)

        return if (normStart <= normEnd) {
            normP in normStart..normEnd
        } else {
            // Wraps around 0.0
            normP >= normStart || normP <= normEnd
        }
    }

    /**
     * Converts a normalized 2D point to actual Canvas coordinates.
     */
    fun toCanvasCoords(point: PointF, squareLeft: Float, squareTop: Float, squareSize: Float): PointF {
        return PointF(
            squareLeft + point.x * squareSize,
            squareTop + point.y * squareSize
        )
    }
}
