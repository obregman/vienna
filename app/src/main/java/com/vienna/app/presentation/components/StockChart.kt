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
import androidx.compose.ui.unit.dp
import com.vienna.app.domain.model.PricePoint
import com.vienna.app.domain.model.TimeRange
import com.vienna.app.presentation.theme.Error
import com.vienna.app.presentation.theme.Success

@Composable
fun StockChart(
    prices: List<PricePoint>,
    modifier: Modifier = Modifier
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

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 8.dp.toPx()

        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)

        if (prices.size < 2) return@Canvas

        val path = Path()
        val fillPath = Path()

        prices.forEachIndexed { index, point ->
            val x = padding + (index.toFloat() / (prices.size - 1)) * chartWidth
            val y = padding + chartHeight - ((point.price - minPrice) / priceRange * chartHeight).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height - padding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Complete fill path
        fillPath.lineTo(padding + chartWidth, height - padding)
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
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
