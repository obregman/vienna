package com.vienna.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import com.vienna.app.domain.model.PricePoint
import com.vienna.app.domain.model.TimeRange
import com.vienna.app.presentation.theme.Error
import com.vienna.app.presentation.theme.Success
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockChart(
    prices: List<PricePoint>,
    modifier: Modifier = Modifier,
    showAxes: Boolean = true,
    timeRange: TimeRange = TimeRange.MONTH_1
) {
    if (prices.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val minPrice = prices.minOf { it.price }
    val maxPrice = prices.maxOf { it.price }
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)

    val firstPrice = prices.first().price
    val lastPrice = prices.last().price
    val isPositive = lastPrice >= firstPrice
    val lineColor = if (isPositive) Success else Error

    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val axisColorInt = axisColor.hashCode()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Increased padding for axis labels
        val leftPadding = if (showAxes) 55.dp.toPx() else 8.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val topPadding = 8.dp.toPx()
        val bottomPadding = if (showAxes) 24.dp.toPx() else 8.dp.toPx()

        val chartWidth = width - leftPadding - rightPadding
        val chartHeight = height - topPadding - bottomPadding

        if (prices.size < 2) return@Canvas

        // Draw axes if enabled
        if (showAxes) {
            val axisPaint = Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10.dp.toPx()
                isAntiAlias = true
            }

            // Draw Y-axis labels (price values)
            val yLabelCount = 4
            for (i in 0..yLabelCount) {
                val price = minPrice + (priceRange * i / yLabelCount)
                val y = topPadding + chartHeight - (chartHeight * i / yLabelCount)

                // Draw horizontal grid line
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw price label
                drawContext.canvas.nativeCanvas.drawText(
                    formatPriceShort(price),
                    4.dp.toPx(),
                    y + 4.dp.toPx(),
                    axisPaint
                )
            }

            // Draw X-axis labels (dates)
            val dateFormat = when (timeRange) {
                TimeRange.DAY_1 -> SimpleDateFormat("HH:mm", Locale.US)
                TimeRange.WEEK_1, TimeRange.MONTH_1 -> SimpleDateFormat("MMM d", Locale.US)
                else -> SimpleDateFormat("MMM yy", Locale.US)
            }

            val xLabelCount = 4
            for (i in 0..xLabelCount) {
                val index = (prices.size - 1) * i / xLabelCount
                val x = leftPadding + (chartWidth * i / xLabelCount)
                val timestamp = prices.getOrNull(index)?.timestamp ?: continue

                // Draw vertical grid line
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(x, topPadding),
                    end = Offset(x, height - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw date label
                val dateLabel = dateFormat.format(Date(timestamp))
                val textWidth = axisPaint.measureText(dateLabel)
                val textX = when (i) {
                    0 -> x
                    xLabelCount -> x - textWidth
                    else -> x - textWidth / 2
                }
                drawContext.canvas.nativeCanvas.drawText(
                    dateLabel,
                    textX,
                    height - 4.dp.toPx(),
                    axisPaint
                )
            }
        }

        val path = Path()
        val fillPath = Path()

        prices.forEachIndexed { index, point ->
            val x = leftPadding + (index.toFloat() / (prices.size - 1)) * chartWidth
            val y = topPadding + chartHeight - ((point.price - minPrice) / priceRange * chartHeight).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height - bottomPadding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Complete fill path
        fillPath.lineTo(leftPadding + chartWidth, height - bottomPadding)
        fillPath.close()

        // Draw fill
        drawPath(
            path = fillPath,
            color = lineColor.copy(alpha = 0.1f)
        )

        // Draw line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun formatPriceShort(price: Double): String {
    return when {
        price >= 1000 -> String.format(Locale.US, "%.0fK", price / 1000)
        price >= 100 -> String.format(Locale.US, "%.0f", price)
        price >= 10 -> String.format(Locale.US, "%.1f", price)
        else -> String.format(Locale.US, "%.2f", price)
    }
}

@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = {
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun StockChartWithSelector(
    prices: List<PricePoint>,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TimeRangeSelector(
            selectedRange = selectedRange,
            onRangeSelected = onRangeSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else {
                StockChart(
                    prices = prices,
                    modifier = Modifier.fillMaxSize(),
                    showAxes = true,
                    timeRange = selectedRange
                )
            }
        }
    }
}

@Composable
fun VolumeChart(
    prices: List<PricePoint>,
    modifier: Modifier = Modifier,
    showAxes: Boolean = true,
    timeRange: TimeRange = TimeRange.MONTH_1
) {
    if (prices.isEmpty() || prices.all { it.volume == 0L }) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No volume data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxVolume = prices.maxOf { it.volume }
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Padding for axis labels
        val leftPadding = if (showAxes) 55.dp.toPx() else 8.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val topPadding = 8.dp.toPx()
        val bottomPadding = if (showAxes) 24.dp.toPx() else 8.dp.toPx()

        val chartWidth = width - leftPadding - rightPadding
        val chartHeight = height - topPadding - bottomPadding

        if (prices.isEmpty()) return@Canvas

        // Draw axes if enabled
        if (showAxes) {
            val axisPaint = Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10.dp.toPx()
                isAntiAlias = true
            }

            // Draw Y-axis labels (volume values)
            val yLabelCount = 3
            for (i in 0..yLabelCount) {
                val volume = (maxVolume * i / yLabelCount)
                val y = topPadding + chartHeight - (chartHeight * i / yLabelCount)

                // Draw horizontal grid line
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw volume label
                drawContext.canvas.nativeCanvas.drawText(
                    formatVolumeShort(volume),
                    4.dp.toPx(),
                    y + 4.dp.toPx(),
                    axisPaint
                )
            }

            // Draw X-axis labels (dates)
            val dateFormat = when (timeRange) {
                TimeRange.DAY_1 -> SimpleDateFormat("HH:mm", Locale.US)
                TimeRange.WEEK_1, TimeRange.MONTH_1 -> SimpleDateFormat("MMM d", Locale.US)
                else -> SimpleDateFormat("MMM yy", Locale.US)
            }

            val xLabelCount = 4
            for (i in 0..xLabelCount) {
                val index = (prices.size - 1) * i / xLabelCount
                val x = leftPadding + (chartWidth * i / xLabelCount)
                val timestamp = prices.getOrNull(index)?.timestamp ?: continue

                // Draw date label
                val dateLabel = dateFormat.format(Date(timestamp))
                val textWidth = axisPaint.measureText(dateLabel)
                val textX = when (i) {
                    0 -> x
                    xLabelCount -> x - textWidth
                    else -> x - textWidth / 2
                }
                drawContext.canvas.nativeCanvas.drawText(
                    dateLabel,
                    textX,
                    height - 4.dp.toPx(),
                    axisPaint
                )
            }
        }

        // Draw volume bars
        val barWidth = (chartWidth / prices.size) * 0.8f
        val barGap = (chartWidth / prices.size) * 0.1f

        prices.forEachIndexed { index, point ->
            if (point.volume > 0 && maxVolume > 0) {
                val barHeight = (point.volume.toFloat() / maxVolume) * chartHeight
                val x = leftPadding + barGap + (index.toFloat() / prices.size) * chartWidth
                val y = topPadding + chartHeight - barHeight

                drawRect(
                    color = barColor.copy(alpha = 0.6f),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
            }
        }
    }
}

private fun formatVolumeShort(volume: Long): String {
    return when {
        volume >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", volume / 1_000_000_000.0)
        volume >= 1_000_000 -> String.format(Locale.US, "%.1fM", volume / 1_000_000.0)
        volume >= 1_000 -> String.format(Locale.US, "%.1fK", volume / 1_000.0)
        else -> volume.toString()
    }
}

@Composable
fun VolumeChartSection(
    prices: List<PricePoint>,
    selectedRange: TimeRange,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Volume",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else {
                VolumeChart(
                    prices = prices,
                    modifier = Modifier.fillMaxSize(),
                    showAxes = true,
                    timeRange = selectedRange
                )
            }
        }
    }
}
