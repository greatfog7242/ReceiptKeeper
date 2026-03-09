package com.receiptkeeper.features.analytics.components

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

data class TreemapNode(
    val label: String,
    val value: Double,
    val color: Int,
    var rect: RectF = RectF()
)

class SquarifiedTreemapGenerator(
    private val targetRatio: Double = 1.0
) {

    /**
     * @param items List of nodes with values (will be sorted automatically)
     * @param bounds The total area to fill (e.g., the Card view size)
     * @return The list of nodes with their calculated [RectF] coordinates
     */
    fun calculateLayout(items: List<TreemapNode>, bounds: RectF): List<TreemapNode> {
        if (items.isEmpty()) return emptyList()

        val sortedItems = items.sortedByDescending { it.value }
        val totalValue = sortedItems.sumOf { it.value }
        val totalArea = bounds.width() * bounds.height()
        
        // Convert raw values to normalized areas relative to the screen bounds
        val areas = sortedItems.map { (it.value / totalValue) * totalArea }

        val result = mutableListOf<TreemapNode>()
        squarify(areas, mutableListOf(), min(bounds.width(), bounds.height()), bounds, sortedItems, 0, result)
        return result
    }

    private fun squarify(
        remainingValues: List<Double>,
        currentRow: MutableList<Double>,
        length: Float,
        bounds: RectF,
        originalNodes: List<TreemapNode>,
        nodeOffset: Int,
        result: MutableList<TreemapNode>
    ) {
        if (remainingValues.isEmpty()) {
            if (currentRow.isNotEmpty()) {
                layoutRow(currentRow, length, bounds, originalNodes, nodeOffset, result)
            }
            return
        }

        val nextValue = remainingValues.first()
        
        if (worstAspectRatio(currentRow + nextValue, length) <= worstAspectRatio(currentRow, length)) {
            // Adding this value makes the row "squarer" (or doesn't hurt), keep going
            currentRow.add(nextValue)
            squarify(remainingValues.drop(1), currentRow, length, bounds, originalNodes, nodeOffset, result)
        } else {
            // Adding it makes it too thin. Finalize this row and start a new one.
            val newBounds = layoutRow(currentRow, length, bounds, originalNodes, nodeOffset, result)
            val newLength = min(newBounds.width(), newBounds.height())
            squarify(remainingValues, mutableListOf(), newLength, newBounds, originalNodes, nodeOffset + currentRow.size, result)
        }
    }

    private fun worstAspectRatio(row: List<Double>, length: Float): Double {
        if (row.isEmpty()) return Double.MAX_VALUE
        val sum = row.sum()
        val rMax = row.maxOrNull() ?: 0.0
        val rMin = row.minOrNull() ?: 0.0
        
        // The aspect ratio cost function
        val aspect1 = (length * length * rMax) / (sum * sum)
        val aspect2 = (sum * sum) / (length * length * rMin)
        val currentWorst = max(aspect1, aspect2)
        
        // Measure distance from target ratio
        return Math.abs(currentWorst - targetRatio)
    }

    private fun layoutRow(
        row: List<Double>,
        length: Float,
        bounds: RectF,
        originalNodes: List<TreemapNode>,
        nodeOffset: Int,
        result: MutableList<TreemapNode>
    ): RectF {
        val rowSum = row.sum()
        val thickness = (rowSum / length).toFloat()
        var currentOffset = 0f

        val isVertical = bounds.width() >= bounds.height()

        for (i in row.indices) {
            val node = originalNodes[nodeOffset + i]
            val area = row[i]
            val step = (area / rowSum * length).toFloat()

            if (isVertical) {
                // Fill vertically, move right
                node.rect = RectF(
                    bounds.left, 
                    bounds.top + currentOffset, 
                    bounds.left + thickness, 
                    bounds.top + currentOffset + step
                )
            } else {
                // Fill horizontally, move down
                node.rect = RectF(
                    bounds.left + currentOffset, 
                    bounds.top, 
                    bounds.left + currentOffset + step, 
                    bounds.top + thickness
                )
            }
            result.add(node)
            currentOffset += step
        }

        // Return the remaining available rectangle
        return if (isVertical) {
            RectF(bounds.left + thickness, bounds.top, bounds.right, bounds.bottom)
        } else {
            RectF(bounds.left, bounds.top + thickness, bounds.right, bounds.bottom)
        }
    }
}