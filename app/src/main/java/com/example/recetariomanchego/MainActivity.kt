package com.example.recetariomanchego

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recetariomanchego.ui.FichaRecetaScreen
import com.example.recetariomanchego.ui.ListaRecetasScreen
import com.example.recetariomanchego.ui.RecetasViewModel
import com.example.recetariomanchego.ui.theme.RecetarioManchegoTheme

// Definimos las rutas de navegación en una sealed class para tenerlas centralizadas
// y no equivocarnos al escribir strings sueltos por el código.
sealed class Screen(val route: String) {
    data object Lista : Screen("lista")
    data object Ficha : Screen("ficha/{id}") {
        // Monta la ruta final sustituyendo {id} por el número de la receta elegida
        fun createRoute(id: Int): String = "ficha/$id"
    }
}

@Composable
fun AppNavigation(
    // Creamos el ViewModel aquí arriba para compartir el mismo estado entre la lista y la ficha
    viewModel: RecetasViewModel = viewModel()
) {
    val navController = rememberNavController()

    // NavHost controla qué pantalla se muestra y mantiene la lista guardada en la pila
    // al navegar a una ficha (así al volver seguimos teniendo el buscador y los filtros tal cual)
    NavHost(
        navController = navController,
        startDestination = Screen.Lista.route
    ) {
        composable(Screen.Lista.route) {
            ListaRecetasScreen(
                viewModel = viewModel,
                onVerFicha = { id ->
                    navController.navigate(Screen.Ficha.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.Ficha.route,
            // Indicamos que el parámetro "id" de la ruta es un número entero
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1

            // Observamos el StateFlow de recetas en tiempo real: así, si pulsamos el corazón
            // dentro de la ficha, Compose repinta la pantalla y cambia el icono al instante
            val recetas by viewModel.recetas.collectAsStateWithLifecycle()
            val receta = recetas.firstOrNull { it.id == id }

            if (receta != null) {
                FichaRecetaScreen(
                    receta = receta,
                    onVolver = { navController.popBackStack() },
                    onAlternarFavorita = { viewModel.alternarFavorita(receta.id) }
                )
            } else {
                // Control de seguridad por si se intenta abrir un id que no existe
                Text(
                    text = stringResource(R.string.receta_no_encontrada),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// Punto de entrada de la aplicación: envuelve toda la interfaz
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecetarioManchegoTheme {
                AppNavigation()
            }
        }
    }
}