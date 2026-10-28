package com.example.lab10

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun SeriesApp() {
    // 1. Retrofit para Django (Series)
    val urlBaseDjango = "http://10.0.2.2:8000/"
    val retrofitDjango = Retrofit.Builder()
        .baseUrl(urlBaseDjango)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val servicioSeries = retrofitDjango.create(SerieApiService::class.java)

    // 2. Retrofit para Google Books (Libros)
    val urlBaseGoogle = "https://www.googleapis.com/"
    val retrofitGoogle = Retrofit.Builder()
        .baseUrl(urlBaseGoogle)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val servicioLibros = retrofitGoogle.create(BookApiService::class.java)

    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.padding(top = 40.dp),
        topBar = { BarraSuperior() },
        bottomBar = { BarraInferior(navController) },
        floatingActionButton = { BotonFAB(navController) },
        content = { paddingValues ->
            Contenido(paddingValues, navController, servicioSeries, servicioLibros)
        }
    )
}

@Composable
fun BotonFAB(navController: NavHostController) {
    val cbeState by navController.currentBackStackEntryAsState()
    val rutaActual = cbeState?.destination?.route
    if (rutaActual == "series") {
        FloatingActionButton(
            containerColor = Color.Magenta,
            contentColor = Color.White,
            onClick = { navController.navigate("serieNuevo") }
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior() {
    CenterAlignedTopAppBar(
        title = { Text(text = "SERIES APP", color = Color.White, fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun BarraInferior(navController: NavHostController) {
    NavigationBar(containerColor = Color.LightGray) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = navController.currentDestination?.route == "inicio",
            onClick = { navController.navigate("inicio") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Favorite, contentDescription = "Series") },
            label = { Text("Series") },
            selected = navController.currentDestination?.route == "series",
            onClick = { navController.navigate("series") }
        )
        // NUEVO BOTÓN DE LIBROS
        NavigationBarItem(
            icon = { Icon(Icons.Filled.MenuBook, contentDescription = "Libros") },
            label = { Text("Libros") },
            selected = navController.currentDestination?.route == "bookshelf",
            onClick = { navController.navigate("bookshelf") }
        )
    }
}

@Composable
fun Contenido(
    pv: PaddingValues,
    navController: NavHostController,
    servicioSeries: SerieApiService,
    servicioLibros: BookApiService
) {
    Box(modifier = Modifier.fillMaxSize().padding(pv)) {
        NavHost(navController = navController, startDestination = "inicio") {
            composable("inicio") { ScreenInicio() }

            // RUTAS DE SERIES (Django)
            composable("series") { ContenidoSeriesListado(navController, servicioSeries) }
            composable("serieNuevo") { ContenidoSerieEditar(navController, servicioSeries, 0) }
            composable("serieVer/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                ContenidoSerieEditar(navController, servicioSeries, it.arguments!!.getInt("id"))
            }
            composable("serieDel/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                ContenidoSerieEliminar(navController, servicioSeries, it.arguments!!.getInt("id"))
            }

            // RUTA DE LIBROS (Google Books)
            composable("bookshelf") { ScreenBookshelf(navController, servicioLibros) }
        }
    }
}

@Composable
fun ScreenInicio() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Bienvenido a la App", fontSize = 20.sp)
    }
}