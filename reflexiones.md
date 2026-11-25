# Reflexión del Proyecto: Recetario Manchego (UT2)

## 1. ¿Qué ocurre si giras el dispositivo estando en la ficha de una receta? ¿Se pierde el estado? ¿Por qué?

Cuando giramos el móvil estando dentro de una receta, Android destruye la `Activity` y la vuelve a crear para cargar el diseño en horizontal (`DisposicionHorizontal`, con la foto al 40 % y los datos al 60 % en lugar de en vertical).

Aun así, **no se pierde ningún dato**:
* Tanto la lista de recetas como la tarjeta que estuviera desplegada se guardan en `RecetasViewModel`, que aguanta los giros de pantalla sin borrarse.
* El `NavController` recuerda el `id` de la receta en la ruta (`ficha/{id}`) dentro de su historial, así que al volver a pintarse la pantalla carga exactamente la misma receta.

---

## 2. ¿Por qué es importante reutilizar bloques de UI en lugar de duplicar código?

Separar la interfaz en bloques independientes (`BloqueFoto`, `BloqueResumen`, `BloqueIngredientes`, `BloquePasos` y `BotonFavorita`) nos ahorra repetir el mismo código en la tarjeta de la lista, en la ficha vertical y en la horizontal. Si más adelante queremos cambiar un margen, el tamaño de un texto o un color de `MaterialTheme.colorScheme`, solo hay que tocarlo en un único sitio y se actualiza en toda la app, evitando fallos y despistes al mantener el código.

---

## 3. ¿Cómo se aplica UDF en este proyecto? Identifica los tres niveles: ViewModel, pantalla y bloques reutilizables.

En este proyecto los datos siempre bajan y las acciones del usuario suben mediante funciones lambda:

1. **ViewModel (`RecetasViewModel`):** Es el que manda sobre los datos. Tiene los estados privados (`_recetas`, `_expandidaId`) para poder cambiarlos por dentro, los saca hacia fuera en modo solo lectura (`StateFlow`) y ofrece métodos públicos (`alternarExpandida(id)`, `alternarFavorita(id)`) para modificarlos de forma controlada.
2. **Pantallas (`ListaRecetasScreen` y `FichaRecetaScreen`):** Hacen de puente. Escuchan los cambios del `ViewModel` con `collectAsStateWithLifecycle()`, le pasan a sus hijos los datos ya preparados (por ejemplo, si una tarjeta está expandida o no) y recogen los clics para llamar a los métodos del `ViewModel`.
3. **Bloques reutilizables (`TarjetaReceta`, `BloqueFoto`, `BotonFavorita`, etc.):** Son componentes sin estado propio (*stateless*) ni acceso al `ViewModel` o al `NavController`. Solo pintan los datos que reciben y avisan hacia arriba con lambdas (`onAlternar`, `onAlternarFavorita`, `onVerFicha`) cuando el usuario pulsa algo.

---

## 4. Buscador por nombre: ¿por qué el texto se guarda con `rememberSaveable` y no en el `ViewModel`? ¿Qué cambiaría si estuviera en el `ViewModel`?

El texto del buscador solo lo usa la pantalla de la lista y no hace falta compartirlo con la ficha, así que con `rememberSaveable` ya sobrevive al giro de pantalla sin meter más código en el `ViewModel`. Si lo guardáramos en el `ViewModel`, el filtro seguiría guardado aunque saliéramos de esa pantalla en la navegación y otras pantallas podrían leerlo o modificarlo.
