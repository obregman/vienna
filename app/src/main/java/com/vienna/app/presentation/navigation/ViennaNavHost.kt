package com.vienna.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vienna.app.presentation.screens.analysis.AnalysisScreen
import com.vienna.app.presentation.screens.errorlog.ErrorLogScreen
import com.vienna.app.presentation.screens.portfolio.PortfolioScreen
import com.vienna.app.presentation.screens.search.SearchScreen
import com.vienna.app.presentation.screens.settings.SettingsScreen
import com.vienna.app.presentation.screens.stockdetail.StockDetailScreen
import com.vienna.app.presentation.screens.stocklist.StockListScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun ViennaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.StockList.route,
        modifier = modifier
    ) {
        composable(NavRoutes.StockList.route) {
            StockListScreen(
                onStockClick = { stock ->
                    navController.navigate(
                        NavRoutes.StockDetail.createRoute(stock.symbol, stock.companyName)
                    )
                },
                onSearchClick = {
                    navController.navigate(NavRoutes.Search.route)
                }
            )
        }

        composable(NavRoutes.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onStockClick = { symbol, name ->
                    navController.navigate(NavRoutes.StockDetail.createRoute(symbol, name))
                }
            )
        }

        composable(NavRoutes.Portfolio.route) {
            PortfolioScreen(
                onStockClick = { symbol, name ->
                    navController.navigate(NavRoutes.StockDetail.createRoute(symbol, name))
                },
                onSearchClick = {
                    navController.navigate(NavRoutes.Search.route)
                }
            )
        }

        composable(
            route = NavRoutes.StockDetail.route,
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType },
                navArgument("companyName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val companyName = backStackEntry.arguments?.getString("companyName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""

            StockDetailScreen(
                onBackClick = { navController.popBackStack() },
                onAnalysisClick = { symbol, name ->
                    navController.navigate(NavRoutes.Analysis.createRoute(symbol, name))
                }
            )
        }

        composable(
            route = NavRoutes.Analysis.route,
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType },
                navArgument("companyName") { type = NavType.StringType }
            )
        ) {
            AnalysisScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                onErrorLogClick = {
                    navController.navigate(NavRoutes.ErrorLog.route)
                }
            )
        }

        composable(NavRoutes.ErrorLog.route) {
            ErrorLogScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
