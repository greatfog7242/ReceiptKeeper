package com.receiptkeeper.features.analytics.components

import android.graphics.RectF

/**
 * Data class for treemap nodes with name, value, and calculated rectangle
 */
data class TreemapNode(
    val name: String,
    val value: Double,
    val color: Int,
    var rect: RectF = RectF()
)

/**
 * Squarified treemap layout algorithm implementation
 * Based on the algorithm by Bruls, Huizing, and van Wijk (2000)
 */
class SquarifiedTreemap {
    
    /**
     * Layout items within the given bounds using squarified algorithm
     * @param items List of nodes to layout (should be sorted descending by value)
     * @param bounds Bounding rectangle for the treemap
     */
    fun layout(items: List<TreemapNode>, bounds: RectF) {
        if (items.isEmpty()) return
        
        // 1. Sort descending (critical for the algorithm)
        val sortedItems = items.sortedByDescending { it.value }
        val totalValue = sortedItems.sumOf { it.value }
        
        if (totalValue == 0.0) return
        
        // 2. Normalize values to the area of the bounds
        val areaScale = (bounds.width() * bounds.height()) / totalValue.toFloat()
        val areas = sortedItems.map { it.value * areaScale }
        
        // 3. Start squarification
        squarify(areas, mutableListOf(), minOf(bounds.width(), bounds.height()), bounds, sortedItems)
    }

    /**
     * Calculate worst aspect ratio for a row of items
     * @param row List of areas in the current row
     * @param width Width available for the row
     * @return Worst aspect ratio in the row
     */
    private fun worstAspectRatio(row: List<Double>, width: Float): Double {
        if (row.isEmpty()) return Double.MAX_VALUE
        val sum = row.sum()
        val max = row.maxOrNull() ?: 0.0
        val min = row.minOrNull() ?: 0.0
        // Formula: max(w^2 * r_max / s^2, s^2 / (w^2 * r_min))
        return maxOf((width * width * max) / (sum * sum), (sum * sum) / (width * width * min))
    }

    /**
     * Recursive squarify algorithm
     * @param areas Remaining areas to layout
     * @param row Current row being built
     * @param w Width available for current row
     * @param bounds Current bounding rectangle
     * @param nodes List of nodes corresponding to areas
     */
    private fun squarify(
        areas: List<Double>,
        row: MutableList<Double>,
        w: Float,
        bounds: RectF,
        nodes: List<TreemapNode>
    ) {
        if (areas.isEmpty()) {
            // Layout the final row
            layoutRow(row, bounds, nodes.take(row.size))
            return
        }

        val nextArea = areas.first()
        val newRow = row + nextArea
        
        if (row.isEmpty() || worstAspectRatio(newRow, w) <= worstAspectRatio(row, w)) {
            // Continue adding to current row
            squarify(areas.drop(1), newRow.toMutableList(), w, bounds, nodes)
        } else {
            // Layout current row and start new row
            layoutRow(row, bounds, nodes.take(row.size))
            
            // Calculate remaining bounds for next row
            val remainingNodes = nodes.drop(row.size)
            val remainingAreas = areas
            
            if (remainingAreas.isNotEmpty()) {
                // Determine new bounds (rotate if needed)
                val newBounds = if (bounds.width() >= bounds.height()) {
                    // Horizontal layout, consume from left/right
                    RectF(
                        bounds.left + bounds.height(),
                        bounds.top,
                        bounds.right,
                        bounds.bottom
                    )
                } else {
                    // Vertical layout, consume from top/bottom
                    RectF(
                        bounds.left,
                        bounds.top + bounds.width(),
                        bounds.right,
                        bounds.bottom
                    )
                }
                val newW = minOf(newBounds.width(), newBounds.height())
                squarify(remainingAreas, mutableListOf(), newW, newBounds, remainingNodes)
            }
        }
    }

    /**
     * Layout a single row of items
     * @param row Areas in the row
     * @param bounds Bounding rectangle for the row
     * @param nodes Nodes corresponding to the row
     */
    private fun layoutRow(row: List<Double>, bounds: RectF, nodes: List<TreemapNode>) {
        if (row.isEmpty() || nodes.isEmpty()) return
        
        val totalArea = row.sum()
        val isHorizontal = bounds.width() >= bounds.height()
        
        var currentPos = 0f
        row.forEachIndexed { index, area ->
            val node = nodes[index]
            
            if (isHorizontal) {
                // Horizontal layout
                val height = bounds.height()
                val width = (area / totalArea) * bounds.width().toDouble()
                node.rect = RectF(
                    bounds.left + currentPos,
                    bounds.top,
                    bounds.left + currentPos + width.toFloat(),
                    bounds.top + height
                )
                currentPos += width.toFloat()
            } else {
                // Vertical layout
                val width = bounds.width()
                val height = (area / totalArea) * bounds.height().toDouble()
                node.rect = RectF(
                    bounds.left,
                    bounds.top + currentPos,
                    bounds.left + width,
                    bounds.top + currentPos + height.toFloat()
                )
                currentPos += height.toFloat()
            }
        }
    }
}