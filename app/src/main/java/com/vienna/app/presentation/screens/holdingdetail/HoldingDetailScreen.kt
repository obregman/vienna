package com.vienna.app.presentation.screens.holdingdetail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.graphics.Paint
import com.vienna.app.domain.model.PricePoint
import com.vienna.app.presentation.components.ErrorState
import com.vienna.app.presentation.components.LoadingIndicator
import com.vienna.app.presentation.components.formatPrice
import com.vienna.app.presentation.theme.Error
import com.vienna.app.presentation.theme.Success
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingDetailScreen(
    onBackClick: () -> Unit,
    viewModel: HoldingDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.holding?.symbol ?: "Holding") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null && uiState.holding == null -> {
                ErrorState(
                    message = uiState.error ?: "Unknown error",
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.holding != null -> {
                val holding = uiState.holding!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header
                    Text(
                        text = holding.companyName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Performance Summary Card
                    PerformanceSummaryCard(
                        purchasePrice = holding.purchasePrice,
                        currentPrice = holding.currentPrice,
                        gainLoss = holding.gainLoss,
                        gainLossPercent = holding.gainLossPercent,
                        isPositive = holding.isPositive
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Purchase Details Card
                    PurchaseDetailsCard(
                        purchaseDate = holding.purchaseDate,
                        purchasePrice = holding.purchasePrice,
                        shares = holding.shares,
                        daysHeld = holding.daysHeld,
                        totalCost = holding.totalCost
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Performance Chart
                    Text(
                        text = "Performance Since Purchase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isChartLoading) {
                            LoadingIndicator()
                        } else {
                            PerformanceChart(
                                prices = uiState.priceHistorySincePurchase,
                                purchasePrice = holding.purchasePrice,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceSummaryCard(
    purchasePrice: Double,
    currentPrice: Double?,
    gainLoss: Double?,
    gainLossPercent: Double?,
    isPositive: Boolean
) {
    val color = if (isPositive) Success else Error
    val sign = if (isPositive) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) {
                Success.copy(alpha = 0.1f)
            } else {
                Error.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Performance",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Current Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPrice(currentPrice ?: purchasePrice),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    gainLossPercent?.let { percent ->
                        Text(
                            text = "$sign${String.format(Locale.US, "%.2f", percent)}%",
                            style = MaterialTheme.typography.headlineSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    gainLoss?.let { gain ->
                        Text(
                            text = "$sign${formatPrice(gain)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseDetailsCard(
    purchaseDate: Long,
    purchasePrice: Double,
    shares: Int,
    daysHeld: Long,
    totalCost: Double
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Purchase Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = "Purchase Date", value = dateFormat.format(Date(purchaseDate)))
            DetailRow(label = "Purchase Price", value = formatPrice(purchasePrice))
            DetailRow(label = "Shares", value = shares.toString())
            DetailRow(label = "Total Cost", value = formatPrice(totalCost))
            DetailRow(label = "Days Held", value = "$daysHeld days")
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PerformanceChart(
    prices: List<PricePoint>,
    purchasePrice: Double,
    modifier: Modifier = Modifier
) {
    if (prices.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No price data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val minPrice = minOf(prices.minOf { it.price }, purchasePrice)
    val maxPrice = maxOf(prices.maxOf { it.price }, purchasePrice)
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)

    val firstPrice = prices.first().price
    val lastPrice = prices.last().price
    val isPositive = lastPrice >= purchasePrice
    val lineColor = if (isPositive) Success else Error
    val purchaseLineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftPadding = 55.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val topPadding = 8.dp.toPx()
        val bottomPadding = 24.dp.toPx()

        val chartWidth = width - leftPadding - rightPadding
        val chartHeight = height - topPadding - bottomPadding

        if (prices.size < 2) return@Canvas

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

        // Draw purchase price reference line
        val purchasePriceY = topPadding + chartHeight - ((purchasePrice - minPrice) / priceRange * chartHeight).toFloat()
        drawLine(
            color = purchaseLineColor.copy(alpha = 0.5f),
            start = Offset(leftPadding, purchasePriceY),
            end = Offset(width - rightPadding, purchasePriceY),
            strokeWidth = 2.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Draw X-axis labels (dates)
        val dateFormat = SimpleDateFormat("MMM d", Locale.US)
        val xLabelCount = 4
        for (i in 0..xLabelCount) {
            val index = (prices.size - 1) * i / xLabelCount
            val x = leftPadding + (chartWidth * i / xLabelCount)
            val timestamp = prices.getOrNull(index)?.timestamp ?: continue

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
