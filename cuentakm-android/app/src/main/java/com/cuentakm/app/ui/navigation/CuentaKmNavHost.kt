package com.cuentakm.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cuentakm.app.domain.model.AuthUser
import com.cuentakm.app.ui.consult.ConsultScreen
import com.cuentakm.app.ui.home.HomeScreen
import com.cuentakm.app.ui.load.LoadScreen

object Routes {
    const val PATENTE_ARG = "patente"
    const val HOME = "home"
    const val CONSULT = "consult/{$PATENTE_ARG}"
    const val LOAD = "load/{$PATENTE_ARG}"

    fun consult(patente: String) = "consult/$patente"
    fun load(patente: String) = "load/$patente"
}

@Composable
fun CuentaKmNavHost(user: AuthUser) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                user = user,
                onConsult = { patente -> navController.navigate(Routes.consult(patente)) },
                onLoad = { patente -> navController.navigate(Routes.load(patente)) },
            )
        }

        composable(
            route = Routes.CONSULT,
            arguments = listOf(navArgument(Routes.PATENTE_ARG) { type = NavType.StringType }),
        ) { entry ->
            val patente = entry.arguments?.getString(Routes.PATENTE_ARG).orEmpty()
            ConsultScreen(
                onBack = { navController.popBackStack() },
                onLoadMaintenance = { navController.navigate(Routes.load(patente)) },
            )
        }

        composable(
            route = Routes.LOAD,
            arguments = listOf(navArgument(Routes.PATENTE_ARG) { type = NavType.StringType }),
        ) {
            LoadScreen(onBack = { navController.popBackStack() })
        }
    }
}
