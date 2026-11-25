package com.example.recetariomanchego.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recetariomanchego.R
import com.example.recetariomanchego.data.Dificultad
import com.example.recetariomanchego.data.Receta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaRecetasScreen(
    viewModel: RecetasViewModel,
    onVerFicha: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Observamos los StateFlow del ViewModel respetando el ciclo de vida (patrón UDF)
    val recetas by viewModel.recetas.collectAsStateWithLifecycle()
    val expandidaId by viewModel.expandidaId.collectAsStateWithLifecycle()

    // Estados propios de esta pantalla: con rememberSaveable aguantan giros
    // y se conservan en la pila de navegación al entrar en una ficha y volver
    var textoBusqueda by rememberSaveable { mutableStateOf("") }
    var soloFavoritas by rememberSaveable { mutableStateOf(false) }
    var dificultadSeleccionada by rememberSaveable { mutableStateOf<Dificultad?>(null) }

    // Filtrado combinado: la receta tiene que cumplir las tres condiciones a la vez
    val recetasFiltradas = recetas.filter { receta ->
        val coincideTexto = textoBusqueda.isBlank() ||
            receta.nombre.contains(textoBusqueda.trim(), ignoreCase = true)
        val coincideFavorita = !soloFavoritas || receta.favorita
        val coincideDificultad = dificultadSeleccionada == null ||
            receta.dificultad == dificultadSeleccionada

        coincideTexto && coincideFavorita && coincideDificultad
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.titulo_lista)) })
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Buscador por nombre en tiempo real
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text(stringResource(R.string.buscar_receta)) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    // Solo mostramos la X para borrar cuando hay texto escrito
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_borrar_busqueda)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Barra de chips sin estado (stateless): recibe valores y sube eventos con lambdas
            BarraFiltrosRecetas(
                soloFavoritas = soloFavoritas,
                onCambiarSoloFavoritas = { soloFavoritas = it },
                dificultadSeleccionada = dificultadSeleccionada,
                onSeleccionarDificultad = { dificultadSeleccionada = it },
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Si ningún plato cumple los filtros activos, mostramos aviso y botón para resetear
            if (recetasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.sin_resultados_busqueda),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                textoBusqueda = ""
                                soloFavoritas = false
                                dificultadSeleccionada = null
                            }
                        ) {
                            Text(stringResource(R.string.filtro_limpiar))
                        }
                    }
                }
            } else {
                // LazyColumn solo renderiza las tarjetas visibles en pantalla.
                // Usar key = { it.id } evita recomposiciones innecesarias al filtrar o expandir
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = recetasFiltradas, key = { it.id }) { receta ->
                        TarjetaReceta(
                            receta = receta,
                            expandida = expandidaId == receta.id,
                            onAlternar = { viewModel.alternarExpandida(receta.id) },
                            onAlternarFavorita = { viewModel.alternarFavorita(receta.id) },
                            onVerFicha = { onVerFicha(receta.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaRecetaScreen(
    receta: Receta,
    onVolver: () -> Unit,
    onAlternarFavorita: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Detectamos la orientación actual del dispositivo para adaptar el diseño
    val configuracion = LocalConfiguration.current
    val esHorizontal = configuracion.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receta.nombre) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                },
                actions = {
                    // Reutilizamos el mismo botón de favorita que en la lista
                    BotonFavorita(
                        favorita = receta.favorita,
                        onAlternarFavorita = onAlternarFavorita
                    )
                }
            )
        }
    ) { padding ->
        if (esHorizontal) {
            DisposicionHorizontal(
                receta = receta,
                modifier = modifier.padding(padding)
            )
        } else {
            DisposicionVertical(
                receta = receta,
                modifier = modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun DisposicionVertical(
    receta: Receta,
    modifier: Modifier = Modifier
) {
    // En vertical todo el conjunto hace scroll como una única columna
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        BloqueFoto(
            receta = receta,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ContenidoReceta(receta = receta)
    }
}

@Composable
private fun DisposicionHorizontal(
    receta: Receta,
    modifier: Modifier = Modifier
) {
    // En horizontal dividimos la pantalla: foto fija a la izquierda (40 %)
    // y columna con scroll independiente a la derecha (60 %)
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BloqueFoto(
            receta = receta,
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        )
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            ContenidoReceta(receta = receta)
        }
    }
}

@Composable
private fun ContenidoReceta(receta: Receta) {
    // Agrupamos los bloques reutilizables para no duplicar código entre vertical y horizontal
    BloqueResumen(receta = receta)
    Spacer(modifier = Modifier.height(16.dp))
    BloqueIngredientes(ingredientes = receta.ingredientes)
    Spacer(modifier = Modifier.height(16.dp))
    BloquePasos(pasos = receta.pasos)
}