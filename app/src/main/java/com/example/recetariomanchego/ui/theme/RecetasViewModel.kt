package com.example.recetariomanchego.ui

import androidx.lifecycle.ViewModel
import com.example.recetariomanchego.data.Receta
import com.example.recetariomanchego.data.recetasIniciales
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// El ViewModel guarda el estado principal de la app y aguanta los giros de pantalla
// sin que se pierdan las recetas favoritas ni la tarjeta que estuviera desplegada.
class RecetasViewModel : ViewModel() {

    // Patrón UDF: el estado mutable (_recetas) es privado para que solo se pueda modificar aquí dentro,
    // y hacia fuera lo sacamos como StateFlow de solo lectura para que las pantallas lo observen.
    private val _recetas = MutableStateFlow(recetasIniciales())
    val recetas: StateFlow<List<Receta>> = _recetas.asStateFlow()

    // Guarda el id de la tarjeta que está abierta en ese momento (null si están todas cerradas)
    private val _expandidaId = MutableStateFlow<Int?>(null)
    val expandidaId: StateFlow<Int?> = _expandidaId.asStateFlow()

    // Si pulsamos la tarjeta que ya estaba abierta la cerramos (null); si pulsamos otra, abrimos la nueva
    // y automáticamente se cierra la anterior porque solo guardamos un único id.
    fun alternarExpandida(id: Int) {
        _expandidaId.value = if (_expandidaId.value == id) null else id
    }

    // Usamos .update y .copy() para generar una lista nueva sin mutar los datos directamente;
    // así Compose detecta el cambio y actualiza el icono del corazón tanto en la lista como en la ficha.
    fun alternarFavorita(id: Int) {
        _recetas.update { lista ->
            lista.map { receta ->
                if (receta.id == id) receta.copy(favorita = !receta.favorita) else receta
            }
        }
    }

    // Método auxiliar para buscar una receta concreta por su id
    fun recetaConId(id: Int): Receta? = _recetas.value.firstOrNull { it.id == id }
}