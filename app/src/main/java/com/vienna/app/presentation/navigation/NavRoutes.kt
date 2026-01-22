package com.vienna.app.presentation.navigation

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class NavRoutes(val route: String) {
    data object StockList : NavRoutes("stock_list")
    data object Search : NavRoutes("search")
    data object Portfolio : NavRoutes("portfolio")
    data object StockDetail : NavRoutes("stock_detail/{symbol}/{companyName}") {
        fun createRoute(symbol: String, companyName: String): String {
            val encodedName = URLEncoder.encode(companyName, StandardCharsets.UTF_8.toString())
            return "stock_detail/$symbol/$encodedName"
        }
    }
    data object Analysis : NavRoutes("analysis/{symbol}/{companyName}") {
        fun createRoute(symbol: String, companyName: String): String {
            val encodedName = URLEncoder.encode(companyName, StandardCharsets.UTF_8.toString())
            return "analysis/$symbol/$encodedName"
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
) {
    data object Stocks : BottomNavItem(NavRoutes.StockList.route, "Stocks", "trending_up")
    data object Portfolio : BottomNavItem(NavRoutes.Portfolio.route, "Portfolio", "account_balance_wallet")
}
