package com.example.lab10

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    // Usamos una lista de estado inmutable para asegurar que se redibuje
    var listaSeries by remember { mutableStateOf<List<SerieModel>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // Obtenemos los datos de la API
            val listado = servicio.selectSeries()
            // Asignamos la lista completa de golpe (esto fuerza el redibujado)
            listaSeries = listado
        } catch (e: Exception) {
            Log.e("ERROR API", "Fallo al conectar: ${e.message}")
        } finally {
            cargando = false
        }
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn {
            item {
                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                    Text(text = "SERIE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                    Text(text = "Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
                }
            }

            // Usamos listaSeries directamente
            items(listaSeries) { item ->
                Row(
                    modifier = Modifier.padding(start=8.dp).fillParentMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier=Modifier.weight(0.1f))
                    Text(text = item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier=Modifier.weight(0.6f))
                    IconButton(
                        onClick = {
                            navController.navigate("serieVer/${item.id}")
                            Log.e("SERIE-VER","ID = ${item.id}")
                        },
                        Modifier.weight(0.1f)
                    ) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver", modifier=Modifier.align(Alignment.CenterVertically))
                    }
                    IconButton(
                        onClick = {
                            navController.navigate("serieDel/${item.id}")
                            Log.e("SERIE-DEL","ID = ${item.id}")
                        },
                        Modifier.weight(0.1f)
                    ) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Ver", modifier=Modifier.align(Alignment.CenterVertically))
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    // CORRECCIÓN: Quitamos los signos de interrogación y el !! para que no se cierre la app
    var id by remember { mutableStateOf(pid) }
    var name by remember { mutableStateOf("") }
    var release_date by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var grabar by remember { mutableStateOf(false) }

    if (id != 0) {
        LaunchedEffect(Unit) {
            val objSerie = servicio.selectSerie(id.toString())
            delay(100)
            // CORRECCIÓN: Si el servidor devuelve null, ponemos un texto vacío en lugar de quebrar la app
            name = objSerie.body()?.name ?: ""
            release_date = objSerie.body()?.release_date ?: ""
            rating = objSerie.body()?.rating.toString() ?: ""
            category = objSerie.body()?.category ?: ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        TextField(value = id.toString(), onValueChange = { }, label = { Text("ID (solo lectura)") }, readOnly = true, singleLine = true)
        // CORRECCIÓN: Quitamos el !! de estos campos
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name: ") }, singleLine = true)
        TextField(value = release_date, onValueChange = { release_date = it }, label = { Text("Release Date:") }, singleLine = true)
        TextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating:") }, singleLine = true)
        TextField(value = category, onValueChange = { category = it }, label = { Text("Category:") }, singleLine = true)

        Button(onClick = { grabar = true }) {
            Text("Grabar", fontSize = 16.sp)
        }
    }

    if (grabar) {
        // CORRECCIÓN: Validamos que el rating sea un número válido. Si no, ponemos 0.
        val ratingInt = rating.toIntOrNull() ?: 0
        val objSerie = SerieModel(id, name, release_date, ratingInt, category)

        LaunchedEffect(Unit) {
            if (id == 0) {
                servicio.insertSerie(objSerie)
            } else {
                servicio.updateSerie(id.toString(), objSerie)
            }
        }
        grabar = false
        navController.navigate("series")
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    var borrar by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            dismissButton = {
                Button(onClick = { showDialog = false; navController.navigate("series") }) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false; borrar = true }) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (borrar) {
        LaunchedEffect(Unit) {
            servicio.deleteSerie(id.toString())
            borrar = false
            navController.navigate("series")
        }
    }
}