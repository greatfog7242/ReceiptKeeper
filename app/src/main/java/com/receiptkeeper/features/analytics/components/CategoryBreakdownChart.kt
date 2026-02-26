package com.receiptkeeper.features.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.domain.model.Category

enum class ChartType {
    TREEMAP,
    PIE,
    STACKED_BAR
}

/**
 * Visual breakdown of spending by category
 * Supports bar chart and pie chart views
 */
@Composable
fun CategoryBreakdownChart(
    categorySpending: List<CategorySpending>,
    categories: List<Category>,
    totalSpending: Double,
    chartType: ChartType = ChartType.TREEMAP,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (categorySpending.isEmpty()) {
                Text(
                    text = "No spending data for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                when (chartType) {
                    ChartType.TREEMAP -> TreeMapChart(categorySpending, categories, totalSpending)
                    ChartType.PIE -> PieChart(categorySpending, categories, totalSpending)
                    ChartType.STACKED_BAR -> StackedBarChart(categorySpending, categories, totalSpending)
                }
            }
        }
    }
}

@Composable
private fun TreeMapChart(
    categorySpending: List<CategorySpending>,
    categories: List<Category>,
    totalSpending: Double
) {
    val total = categorySpending.sumOf { it.total }

    if (categorySpending.isEmpty() || total == 0.0) {
        Text(
            text = "No spending data",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    // Simple treemap layout algorithm
    val rectangles = calculateTreemap(categorySpending, categories, total)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                rectangles.forEach { rect ->
                    drawRect(
                        color = Color(rect.color),
                        topLeft = Offset(rect.x, rect.y),
                        size = Size(rect.width, rect.height)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            categorySpending.forEach { spending ->
                val category = categories.find { it.id == spending.categoryId }
                if (category != null) {
                    val percentage = if (totalSpending > 0) (spending.total / totalSpending * 100).toInt() else 0
                    val categoryColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private data class TreemapRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Int,
    val label: String
)

private fun calculateTreemap(
    categorySpending: List<CategorySpending>,
    categories: List<Category>,
    total: Double
): List<TreemapRect> {
    if (categorySpending.isEmpty() || total == 0.0) return emptyList()

    val width = 300f
    val height = 160f

    // Sort by amount descending
    val sorted = categorySpending.sortedByDescending { it.total }
    val rectangles = mutableListOf<TreemapRect>()

    var currentX = 0f
    var currentY = 0f
    var remainingWidth = width
    var remainingHeight = height
    var isHorizontal = true

    sorted.forEachIndexed { index, spending ->
        val ratio = (spending.total / total).toFloat()
        val category = categories.find { it.id == spending.categoryId }
        val color = category?.let {
            try {
                android.graphics.Color.parseColor(it.colorHex)
            } catch (e: Exception) {
                android.graphics.Color.GRAY
            }
        } ?: android.graphics.Color.GRAY

        val rectWidth: Float
        val rectHeight: Float

        if (isHorizontal) {
            rectWidth = remainingWidth * ratio
            rectHeight = height
            rectangles.add(TreemapRect(currentX, currentY, rectWidth, rectHeight, color, category?.name ?: ""))

            currentX += rectWidth
            remainingWidth -= rectWidth
        } else {
            rectWidth = width
            rectHeight = remainingHeight * ratio
            rectangles.add(TreemapRect(currentX, currentY, rectWidth, rectHeight, color, category?.name ?: ""))

            currentY += rectHeight
            remainingHeight -= rectHeight
        }

        // Alternate direction
        if (index % 2 == 1) isHorizontal = !isHorizontal
    }

    return rectangles
}

@Composable
private fun PieChart(
    categorySpending: List<CategorySpending>,
    categories: List<Category>,
    totalSpending: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pie chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                drawPieChart(categorySpending, categories)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorySpending.forEach { spending ->
                val category = categories.find { it.id == spending.categoryId }
                if (category != null) {
                    val percentage = if (totalSpending > 0) (spending.total / totalSpending * 100).toInt() else 0
                    val categoryColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${"%.2f".format(spending.total)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPieChart(
    categorySpending: List<CategorySpending>,
    categories: List<Category>
) {
    val total = categorySpending.sumOf { it.total }
    if (total == 0.0) return

    val radius = size.minDimension / 2
    val center = Offset(size.width / 2, size.height / 2)

    var startAngle = -90f

    categorySpending.forEach { spending ->
        val sweepAngle = (spending.total.toFloat() / total.toFloat()) * 360f
        val category = categories.find { it.id == spending.categoryId }
        val color = category?.let {
            try {
                android.graphics.Color.parseColor(it.colorHex)
            } catch (e: Exception) {
                android.graphics.Color.GRAY
            }
        } ?: android.graphics.Color.GRAY

        drawArc(
            color = Color(color),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        startAngle += sweepAngle
    }
}

@Composable
private fun StackedBarChart(
    categorySpending: List<CategorySpending>,
    categories: List<Category>,
    totalSpending: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stacked horizontal bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val total = categorySpending.sumOf { it.total }
                if (total == 0.0) return@Canvas

                var startX = 0f
                val barHeight = size.height
                val cornerRadius = 8f

                categorySpending.forEach { spending ->
                    val width = (spending.total.toFloat() / total.toFloat()) * size.width
                    val category = categories.find { it.id == spending.categoryId }
                    val color = category?.let {
                        try {
                            android.graphics.Color.parseColor(it.colorHex)
                        } catch (e: Exception) {
                            android.graphics.Color.GRAY
                        }
                    } ?: android.graphics.Color.GRAY

                    // Draw segment
                    drawRoundRect(
                        color = Color(color),
                        topLeft = Offset(startX, 0f),
                        size = Size(width, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            if (startX == 0f) cornerRadius else 0f,
                            if (startX == 0f) cornerRadius else 0f
                        )
                    )

                    startX += width
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorySpending.forEach { spending ->
                val category = categories.find { it.id == spending.categoryId }
                if (category != null) {
                    val percentage = if (totalSpending > 0) (spending.total / totalSpending * 100).toInt() else 0
                    val categoryColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${"%.2f".format(spending.total)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual category spending item with progress bar
 */
@Composable
private fun CategorySpendingItem(
    category: Category,
    amount: Double,
    totalSpending: Double,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalSpending > 0) (amount / totalSpending * 100).toInt() else 0
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${"%.2f".format(amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = categoryColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
