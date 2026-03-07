package com.receiptkeeper.features.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
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
import com.receiptkeeper.core.util.IconHelper
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.domain.model.Category
import kotlin.math.max

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
    if (total == 0.0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No spending data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        return
    }

    // Aggregate small categories (<10%) into "Other" for tree view only
    val aggregatedData = aggregateSmallCategoriesForTreeView(categorySpending, totalSpending)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Calculate tree map with actual canvas dimensions
                val rectangles = calculateTreemap(aggregatedData, categories, total, size.width, size.height)
                
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
            // Calculate adjusted percentages that sum to 100%
            val adjustedPercentages = calculateAdjustedPercentages(aggregatedData, totalSpending)
            
            aggregatedData.forEachIndexed { index, spending ->
                val isOtherCategory = spending.categoryId == -1L
                val category = if (!isOtherCategory) categories.find { it.id == spending.categoryId } else null
                val percentage = adjustedPercentages[index]
                val categoryColor = if (!isOtherCategory && category != null) {
                    try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                } else {
                    // Gray color for "Other" category
                    MaterialTheme.colorScheme.outline
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
                        Icon(
                            imageVector = if (!isOtherCategory && category != null) {
                                IconHelper.getIconWithTheme(category.iconName)
                            } else {
                                // MoreHoriz icon for "Other" category
                                Icons.Default.MoreHoriz
                            },
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (!isOtherCategory && category != null) {
                                category.name
                            } else {
                                "Other"
                            },
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

private data class TreemapRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Int,
    val label: String
)

/**
 * Aggregates categories with less than 10% of total spending into "Other" category
 * "Other" category ID is -1 to distinguish it from real categories
 * Only for tree view - other chart types show individual categories
 */
private fun aggregateSmallCategoriesForTreeView(
    categorySpendingList: List<CategorySpending>,
    totalSpending: Double
): List<CategorySpending> {
    if (categorySpendingList.isEmpty()) return emptyList()
    
    if (totalSpending == 0.0) return emptyList()
    
    // Calculate percentage for each category
    val categoriesWithPercentage = categorySpendingList.map { spending ->
        val percentage = (spending.total / totalSpending) * 100
        spending to percentage
    }
    
    // Separate categories above and below 10%
    val (majorCategories, minorCategories) = categoriesWithPercentage.partition { (_, percentage) -> 
        percentage >= 10.0 
    }
    
    // If no minor categories, return original list
    if (minorCategories.isEmpty()) {
        return categorySpendingList
    }
    
    // Calculate total for major categories
    val majorTotal = majorCategories.sumOf { (spending, _) -> spending.total }
    
    // Create "Other" category with remaining total
    val otherTotal = totalSpending - majorTotal
    
    // Build result list with major categories and "Other"
    val result = mutableListOf<CategorySpending>()
    result.addAll(majorCategories.map { (spending, _) -> spending })
    
    if (otherTotal > 0.0) {
        // Use -1 as ID for "Other" category
        result.add(CategorySpending(categoryId = -1, total = otherTotal))
    }
    
    return result
}

/**
 * Calculates adjusted percentages that sum to exactly 100%
 * Uses largest remainder method to handle rounding errors
 */
private fun calculateAdjustedPercentages(
    spendingList: List<CategorySpending>,
    totalSpending: Double
): List<Int> {
    if (spendingList.isEmpty() || totalSpending == 0.0) {
        return List(spendingList.size) { 0 }
    }
    
    // Calculate raw percentages with decimals
    val rawPercentages = spendingList.map { spending ->
        (spending.total / totalSpending * 100)
    }
    
    // Round down to get integer parts
    val integerParts = rawPercentages.map { it.toInt() }
    val remainders = rawPercentages.mapIndexed { index, raw -> 
        raw - integerParts[index] 
    }
    
    // Calculate total of integer parts
    var totalIntegerParts = integerParts.sum()
    
    // Distribute remaining percentage points to items with largest remainders
    val sortedByRemainder = remainders
        .mapIndexed { index, remainder -> index to remainder }
        .sortedByDescending { (_, remainder) -> remainder }
    
    val adjusted = integerParts.toMutableList()
    var remainingPoints = 100 - totalIntegerParts
    
    // Add 1 to items with largest remainders until we reach 100%
    for ((index, _) in sortedByRemainder) {
        if (remainingPoints > 0) {
            adjusted[index] += 1
            remainingPoints -= 1
        } else {
            break
        }
    }
    
    return adjusted
}

private fun calculateTreemap(
    categorySpending: List<CategorySpending>,
    categories: List<Category>,
    total: Double,
    canvasWidth: Float,
    canvasHeight: Float
): List<TreemapRect> {
    if (categorySpending.isEmpty() || total == 0.0) return emptyList()

    // Add padding around the tree map for centering
    val padding = 8f
    val width = canvasWidth - 2 * padding
    val height = canvasHeight - 2 * padding
    val totalArea = width * height

    // Sort by amount descending
    val sorted = categorySpending.sortedByDescending { it.total }
    val rectangles = mutableListOf<TreemapRect>()

    // Calculate percentages and target areas
    val itemsWithPercentages = sorted.map { spending ->
        val percentage = spending.total / total
        val targetArea = totalArea * percentage.toFloat()
        Triple(spending, percentage, targetArea)
    }

    // Simple algorithm: always use full dimensions for each slice
    var currentX = padding
    var currentY = padding
    var remainingWidth = width
    var remainingHeight = height

    itemsWithPercentages.forEachIndexed { index, (spending, percentage, targetArea) ->
        val isOtherCategory = spending.categoryId == -1L
        val category = if (!isOtherCategory) categories.find { it.id == spending.categoryId } else null
        val color = if (!isOtherCategory && category != null) {
            try {
                android.graphics.Color.parseColor(category.colorHex)
            } catch (e: Exception) {
                android.graphics.Color.GRAY
            }
        } else {
            // Gray color for "Other" category
            android.graphics.Color.GRAY
        }

        val rectWidth: Float
        val rectHeight: Float

        // Decide direction based on which dimension gives better aspect ratio
        // For horizontal slice: width = targetArea / remainingHeight
        val horizontalWidth = targetArea / remainingHeight
        val horizontalAspectRatio = max(horizontalWidth / remainingHeight, remainingHeight / horizontalWidth)
        
        // For vertical slice: height = targetArea / remainingWidth  
        val verticalHeight = targetArea / remainingWidth
        val verticalAspectRatio = max(remainingWidth / verticalHeight, verticalHeight / remainingWidth)
        
        // Choose the direction that gives more square-like rectangle (lower aspect ratio)
        val useHorizontal = horizontalAspectRatio <= verticalAspectRatio

        if (useHorizontal) {
            // Horizontal slice: full remaining height, calculated width
            rectHeight = remainingHeight
            rectWidth = targetArea / rectHeight
            rectangles.add(TreemapRect(currentX, currentY, rectWidth, rectHeight, color, 
                if (!isOtherCategory && category != null) category.name else "Other"))

            // Update for next item
            currentX += rectWidth
            remainingWidth -= rectWidth
        } else {
            // Vertical slice: full remaining width, calculated height
            rectWidth = remainingWidth
            rectHeight = targetArea / rectWidth
            rectangles.add(TreemapRect(currentX, currentY, rectWidth, rectHeight, color, 
                if (!isOtherCategory && category != null) category.name else "Other"))

            // Update for next item
            currentY += rectHeight
            remainingHeight -= rectHeight
        }
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
                            Icon(
                                imageVector = IconHelper.getIconWithTheme(category.iconName),
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(18.dp)
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
                            Icon(
                                imageVector = IconHelper.getIconWithTheme(category.iconName),
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(18.dp)
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
