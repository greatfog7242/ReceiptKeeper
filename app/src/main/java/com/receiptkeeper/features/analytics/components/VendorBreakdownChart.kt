package com.receiptkeeper.features.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.receiptkeeper.core.util.IconHelper
import com.receiptkeeper.data.local.entity.VendorSpending
import com.receiptkeeper.domain.model.Vendor

// Default colors for vendors without brand icons
private val vendorColors = listOf(
    "#4285F4", "#DB4437", "#F4B400", "#0F9D58",
    "#AB47BC", "#00ACC1", "#FF7043", "#8D6E63",
    "#78909C", "#5C6BC0", "#26A69A", "#EC407A"
)

/**
 * Visual breakdown of spending by vendor
 * Supports bar chart, pie chart, and treemap views
 */
@Composable
fun VendorBreakdownChart(
    vendorSpending: List<VendorSpending>,
    vendors: List<Vendor>,
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
                text = "Spending by Vendor",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (vendorSpending.isEmpty()) {
                Text(
                    text = "No spending data for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                when (chartType) {
                    ChartType.TREEMAP -> VendorTreeMapChart(vendorSpending, vendors, totalSpending)
                    ChartType.PIE -> VendorPieChart(vendorSpending, vendors, totalSpending)
                    ChartType.STACKED_BAR -> VendorStackedBarChart(vendorSpending, vendors, totalSpending)
                }
            }
        }
    }
}

@Composable
private fun VendorTreeMapChart(
    vendorSpending: List<VendorSpending>,
    vendors: List<Vendor>,
    totalSpending: Double
) {
    val total = vendorSpending.sumOf { it.total }

    if (vendorSpending.isEmpty() || total == 0.0) {
        Text(
            text = "No spending data",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    // Aggregate small vendors (<10%) into "Other" for tree view only
    val aggregatedData = aggregateSmallVendorsForTreeView(vendorSpending, total)
    
    val rectangles = calculateVendorTreemap(aggregatedData, vendors, total)

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
            // Calculate adjusted percentages that sum to 100%
            val adjustedPercentages = calculateAdjustedVendorPercentages(aggregatedData, totalSpending)
            
            aggregatedData.forEachIndexed { index, spending ->
                val isOtherVendor = spending.vendorId == -1L
                val vendor = if (!isOtherVendor) vendors.find { it.id == spending.vendorId } else null
                val percentage = adjustedPercentages[index]
                val colorIndex = index % vendorColors.size
                val vendorColor = if (!isOtherVendor) {
                    try {
                        Color(android.graphics.Color.parseColor(vendorColors[colorIndex]))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                } else {
                    // Gray color for "Other" vendor
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
                        if (!isOtherVendor && vendor != null) {
                            VendorIcon(
                                vendor = vendor,
                                size = 16.dp,
                                tint = vendorColor
                            )
                        } else {
                            // MoreHoriz icon for "Other" vendor
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = vendorColor
                            )
                        }
                        Text(
                            text = if (!isOtherVendor && vendor != null) {
                                vendor.name
                            } else {
                                "Other"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = vendorColor
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

private data class VendorTreemapRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Int,
    val label: String
)

/**
 * Aggregates vendors with less than 10% of total spending into "Other" vendor
 * "Other" vendor ID is -1 to distinguish it from real vendors
 * Only for tree view - other chart types show individual vendors
 */
private fun aggregateSmallVendorsForTreeView(
    vendorSpendingList: List<VendorSpending>,
    totalSpending: Double
): List<VendorSpending> {
    if (vendorSpendingList.isEmpty()) return emptyList()
    
    if (totalSpending == 0.0) return emptyList()
    
    // Calculate percentage for each vendor
    val vendorsWithPercentage = vendorSpendingList.map { spending ->
        val percentage = (spending.total / totalSpending) * 100
        spending to percentage
    }
    
    // Separate vendors above and below 10%
    val (majorVendors, minorVendors) = vendorsWithPercentage.partition { (_, percentage) -> 
        percentage >= 10.0 
    }
    
    // If no minor vendors, return original list
    if (minorVendors.isEmpty()) {
        return vendorSpendingList
    }
    
    // Calculate total for major vendors
    val majorTotal = majorVendors.sumOf { (spending, _) -> spending.total }
    
    // Create "Other" vendor with remaining total
    val otherTotal = totalSpending - majorTotal
    
    // Build result list with major vendors and "Other"
    val result = mutableListOf<VendorSpending>()
    result.addAll(majorVendors.map { (spending, _) -> spending })
    
    if (otherTotal > 0.0) {
        // Use -1 as ID for "Other" vendor
        result.add(VendorSpending(vendorId = -1, total = otherTotal))
    }
    
    return result
}

/**
 * Calculates adjusted percentages that sum to exactly 100%
 * Uses largest remainder method to handle rounding errors
 */
private fun calculateAdjustedVendorPercentages(
    spendingList: List<VendorSpending>,
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

private fun calculateVendorTreemap(
    vendorSpending: List<VendorSpending>,
    vendors: List<Vendor>,
    total: Double
): List<VendorTreemapRect> {
    if (vendorSpending.isEmpty() || total == 0.0) return emptyList()

    val width = 300f
    val height = 160f

    val sorted = vendorSpending.sortedByDescending { it.total }
    val rectangles = mutableListOf<VendorTreemapRect>()

    var currentX = 0f
    var currentY = 0f
    var remainingWidth = width
    var remainingHeight = height
    var isHorizontal = true

    sorted.forEachIndexed { index, spending ->
        val ratio = (spending.total / total).toFloat()
        val isOtherVendor = spending.vendorId == -1L
        val vendor = if (!isOtherVendor) vendors.find { it.id == spending.vendorId } else null
        val colorIndex = index % vendorColors.size
        val color = if (!isOtherVendor) {
            try {
                android.graphics.Color.parseColor(vendorColors[colorIndex])
            } catch (e: Exception) {
                android.graphics.Color.GRAY
            }
        } else {
            // Gray color for "Other" vendor
            android.graphics.Color.GRAY
        }

        val rectWidth: Float
        val rectHeight: Float

        if (isHorizontal) {
            rectWidth = remainingWidth * ratio
            rectHeight = remainingHeight
            rectangles.add(VendorTreemapRect(currentX, currentY, rectWidth, rectHeight, color, 
                if (!isOtherVendor && vendor != null) vendor.name else "Other"))

            currentX += rectWidth
            remainingWidth -= rectWidth
        } else {
            rectWidth = remainingWidth
            rectHeight = remainingHeight * ratio
            rectangles.add(VendorTreemapRect(currentX, currentY, rectWidth, rectHeight, color, 
                if (!isOtherVendor && vendor != null) vendor.name else "Other"))

            currentY += rectHeight
            remainingHeight -= rectHeight
        }

        if (index % 2 == 1) isHorizontal = !isHorizontal
    }

    return rectangles
}

@Composable
private fun VendorPieChart(
    vendorSpending: List<VendorSpending>,
    vendors: List<Vendor>,
    totalSpending: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                drawVendorPieChart(vendorSpending, vendors)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vendorSpending.forEachIndexed { index, spending ->
                val vendor = vendors.find { it.id == spending.vendorId }
                if (vendor != null) {
                    val percentage = if (totalSpending > 0) (spending.total / totalSpending * 100).toInt() else 0
                    val colorIndex = index % vendorColors.size
                    val vendorColor = try {
                        Color(android.graphics.Color.parseColor(vendorColors[colorIndex]))
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
                            VendorIcon(
                                vendor = vendor,
                                size = 18.dp,
                                tint = vendorColor
                            )
                            Text(
                                text = vendor.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = vendorColor
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

private fun DrawScope.drawVendorPieChart(
    vendorSpending: List<VendorSpending>,
    vendors: List<Vendor>
) {
    val total = vendorSpending.sumOf { it.total }
    if (total == 0.0) return

    val radius = size.minDimension / 2
    val center = Offset(size.width / 2, size.height / 2)

    var startAngle = -90f

    vendorSpending.forEachIndexed { index, spending ->
        val sweepAngle = (spending.total.toFloat() / total.toFloat()) * 360f
        val colorIndex = index % vendorColors.size
        val color = try {
            android.graphics.Color.parseColor(vendorColors[colorIndex])
        } catch (e: Exception) {
            android.graphics.Color.GRAY
        }

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
private fun VendorStackedBarChart(
    vendorSpending: List<VendorSpending>,
    vendors: List<Vendor>,
    totalSpending: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val total = vendorSpending.sumOf { it.total }
                if (total == 0.0) return@Canvas

                var startX = 0f
                val barHeight = size.height
                val cornerRadius = 8f

                vendorSpending.forEachIndexed { index, spending ->
                    val width = (spending.total.toFloat() / total.toFloat()) * size.width
                    val colorIndex = index % vendorColors.size
                    val color = try {
                        android.graphics.Color.parseColor(vendorColors[colorIndex])
                    } catch (e: Exception) {
                        android.graphics.Color.GRAY
                    }

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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vendorSpending.forEachIndexed { index, spending ->
                val vendor = vendors.find { it.id == spending.vendorId }
                if (vendor != null) {
                    val percentage = if (totalSpending > 0) (spending.total / totalSpending * 100).toInt() else 0
                    val colorIndex = index % vendorColors.size
                    val vendorColor = try {
                        Color(android.graphics.Color.parseColor(vendorColors[colorIndex]))
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
                            VendorIcon(
                                vendor = vendor,
                                size = 18.dp,
                                tint = vendorColor
                            )
                            Text(
                                text = vendor.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = vendorColor
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
 * Composable to display vendor icon - brand logo if available, fallback to icon
 */
@Composable
private fun VendorIcon(
    vendor: Vendor,
    size: androidx.compose.ui.unit.Dp,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (IconHelper.isBrandIcon(vendor.iconName)) {
        // It's a brand icon - load the image
        val imageModel = if (IconHelper.isCustomIcon(vendor.iconName)) {
            IconHelper.getCustomBrandIconUri(context, vendor.iconName)
        } else {
            val brandIconName = IconHelper.getBrandIconName(vendor.iconName)
            "file:///android_asset/brand_logos/${brandIconName}.png"
        }
        val model = ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .build()

        AsyncImage(
            model = model,
            contentDescription = vendor.name,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else {
        // Use material icon
        Icon(
            imageVector = IconHelper.getIcon(vendor.iconName),
            contentDescription = null,
            modifier = modifier.size(size),
            tint = tint
        )
    }
}
