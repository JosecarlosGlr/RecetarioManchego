package com.example.recetariomanchego.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recetariomanchego.R
import com.example.recetariomanchego.data.Dificultad
import com.example.recetariomanchego.data.Receta
import com.example.recetariomanchego.data.recetasIniciales
import com.example.recetariomanchego.ui.theme.RecetarioManchegoTheme

// Todos los componentes de este archivo son stateless (sin estado propio):
// reciben los datos ya preparados y avisan hacia arriba con lambdas cuando el usuario pulsa algo.

@Composable
fun BarraFiltrosRecetas(
    soloFavoritas: Boolean,
    onCambiarSoloFavoritas: (Boolean) -> Unit,
    dificultadSeleccionada: Dificultad?,
    onSeleccionarDificultad: (Dificultad?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Usamos LazyRow para que los chips se puedan deslizar en horizontal si la pantalla es estrecha
    // o si los textos ocupan más al cambiar el móvil a inglés
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip para filtrar solo las recetas marcadas como favoritas
        item {
            FilterChip(
                selected = soloFavoritas,
                onClick = { onCambiarSoloFavoritas(!soloFavoritas) },
                label = { Text(stringResource(R.string.filtro_solo_favoritas)) },
                leadingIcon = if (soloFavoritas) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }

        // Generamos un chip por cada nivel del enum Dificultad (Fácil, Media, Difícil)
        items(Dificultad.entries) { dificultad ->
            val seleccionada = dificultadSeleccionada == dificultad
            FilterChip(
                selected = seleccionada,
                onClick = {
                    // Si volvemos a pulsar la dificultad que ya estaba marcada, la desactivamos (null)
                    onSeleccionarDificultad(if (seleccionada) null else dificultad)
                },
                label = {
                    Text(stringResource(dificultad.etiquetaRes))
                },
                leadingIcon = if (seleccionada) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun BloqueFoto(
    receta: Receta,
    modifier: Modifier = Modifier
) {
    // Bloque reutilizable para la foto: se usa tanto en pequeño en la tarjeta
    // como en grande en las disposiciones vertical y horizontal de la ficha
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Image(
            painter = painterResource(id = receta.imagenId),
            contentDescription = stringResource(R.string.cd_foto_receta, receta.nombre),
            // ContentScale.Crop recorta lo que sobre para rellenar el hueco sin deformar la foto
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BloqueResumen(
    receta: Receta,
    modifier: Modifier = Modifier
) {
    // Muestra en una línea el tiempo, la dificultad traducida y la comarca
    Text(
        text = stringResource(
            R.string.resumen_receta,
            receta.tiempoMinutos,
            stringResource(receta.dificultad.etiquetaRes),
            receta.comarca
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BloqueIngredientes(
    ingredientes: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.titulo_ingredientes),
            style = MaterialTheme.typography.titleMedium
        )
        // Pintamos cada ingrediente con un icono de verificación delante
        ingredientes.forEach { ingrediente ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = ingrediente,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun BloquePasos(
    pasos: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.titulo_elaboracion),
            style = MaterialTheme.typography.titleMedium
        )
        // Usamos forEachIndexed para numerar los pasos automáticamente (index + 1) dentro de un círculo
        pasos.forEachIndexed { index, paso ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Text(
                    text = paso,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TarjetaReceta(
    receta: Receta,
    expandida: Boolean,
    onAlternar: () -> Unit,
    onAlternarFavorita: () -> Unit,
    onVerFicha: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animamos el cambio de color de fondo cuando la tarjeta pasa de cerrada a expandida
    val colorTarjeta by animateColorAsState(
        targetValue = if (expandida) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "colorTarjetaReceta"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Card(
            onClick = onAlternar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorTarjeta,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BloqueFoto(receta = receta, modifier = Modifier.size(72.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receta.nombre,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.resumen_corto,
                            receta.tiempoMinutos,
                            stringResource(receta.dificultad.etiquetaRes)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Al ser un IconButton independiente, captura su propio clic sin abrir ni cerrar la tarjeta
                BotonFavorita(
                    favorita = receta.favorita,
                    onAlternarFavorita = onAlternarFavorita
                )
            }
        }

        // Despliega suavemente la comarca y el botón de acceso cuando expandida es true
        AnimatedVisibility(
            visible = expandida,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.comarca_formato, receta.comarca),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onVerFicha,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ver_receta_completa))
                }
            }
        }
    }
}

// Previews múltiples para comprobar rápido en Android Studio el tema claro, oscuro y la traducción al inglés
@Preview(showBackground = true, name = "Claro")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Oscuro"
)
@Preview(
    showBackground = true,
    locale = "en",
    name = "Inglés"
)
@Composable
private fun TarjetaRecetaPreview() {
    RecetarioManchegoTheme {
        TarjetaReceta(
            receta = recetasIniciales().first(),
            expandida = true,
            onAlternar = {},
            onAlternarFavorita = {},
            onVerFicha = {}
        )
    }
}

@Composable
fun BotonFavorita(
    favorita: Boolean,
    onAlternarFavorita: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Reutilizamos este botón tanto en TarjetaReceta como en la TopAppBar de FichaRecetaScreen
    val colorCorazon by animateColorAsState(
        targetValue = if (favorita) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "colorCorazonFavorita"
    )

    IconButton(
        onClick = onAlternarFavorita,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (favorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            // Cambiamos la descripción accesible según el estado actual para lectores de pantalla
            contentDescription = stringResource(
                if (favorita) R.string.cd_quitar_favorita else R.string.cd_marcar_favorita
            ),
            tint = colorCorazon
        )
    }
}