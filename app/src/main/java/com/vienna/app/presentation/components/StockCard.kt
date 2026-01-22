package com.vienna.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vienna.app.domain.model.Stock
import com.vienna.app.presentation.theme.Error
import com.vienna.app.presentation.theme.Success
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StockCard(
    stock: Stock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.companyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Vol: ${formatVolume(stock.volume)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatPrice(stock.currentPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                PriceChangeBadge(
                    change = stock.priceChange,
                    percentChange = stock.percentChange,
                    isPositive = stock.isPositiveChange
                )
            }
        }
    }
}

@Composable
fun PriceChangeBadge(
    change: Double,
    percentChange: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) Success else Error
    val sign = if (isPositive) "+" else ""

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$sign${formatPrice(change)}",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "(${sign}${String.format(Locale.US, "%.2f", percentChange)}%)",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(price)
}

fun formatVolume(volume: Long): String {
    return when {
        volume >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", volume / 1_000_000_000.0)
        volume >= 1_000_000 -> String.format(Locale.US, "%.2fM", volume / 1_000_000.0)
        volume >= 1_000 -> String.format(Locale.US, "%.2fK", volume / 1_000.0)
        else -> volume.toString()
    }
}
