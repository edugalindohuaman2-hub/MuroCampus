# Bitácora de Prompts y Análisis Técnico - MuroCampus (Fase 2: mejora-ia)

Este documento registra la bitácora de prompts utilizados durante la Fase 2 del laboratorio **MuroCampus** y el análisis técnico real correspondiente a las 3 mejoras fundamentales, validando el cumplimiento estricto de la arquitectura **MVVM + Repository**.

---

## Tabla de Registro de Prompts y Decisiones Técnicas

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
| :--- | :--- | :--- | :--- |
| **Mejora 1: Interceptor OkHttp (`Accept: application/json`)**<br><br>`En com.galindo.murocampus.data.remote.RetrofitClient, agrega un Interceptor de OkHttp (antes del HttpLoggingInterceptor) que intercepte cada petición y agregue el encabezado Accept: application/json usando chain.proceed(chain.request().newBuilder().header("Accept", "application/json").build()). Muéstrame el archivo completo.` | Sugirió declarar un objeto `Interceptor` explícito en una variable privada e insertarlo en el `OkHttpClient.Builder()` antes de `HttpLoggingInterceptor`. | Se simplificó la sintaxis utilizando una lambda inline (`.addInterceptor { chain -> ... }`) dentro de `OkHttpClient.Builder()`. | Mantiene la configuración del cliente HTTP concisa e idiomática en Kotlin. Colocar el interceptor antes de `HttpLoggingInterceptor` garantiza que los logs reflejen el encabezado `Accept: application/json` ya inyectado en cada petición saliente. |
| **Mejora 2: Buscador Local Instantáneo (`PostsViewModel.kt` y `PantallaLista.kt`)**<br><br>`Implementa una búsqueda local por título en com.galindo.murocampus sin hacer peticiones extra a internet: En PostsViewModel.kt, guarda en memoria privada la lista original descargada (private var listaOriginal: List<Post> = emptyList()), agrega private val _consulta = MutableStateFlow("") y val consulta = _consulta.asStateFlow(), y crea fun buscar(texto: String) que actualice _consulta.value = texto y filtre listaOriginal por title.contains(texto, ignoreCase = true), emitiendo el nuevo UiState.Exito(filtrada) (sin tocar Retrofit ni la API). Cuando cargar() termine con éxito, guarda la lista en listaOriginal y aplica el filtro actual si existe. En PantallaLista.kt, agrega un OutlinedTextField compacto debajo de los chips de usuario con placeholder "Buscar por título..." y un botón de limpiar cuando haya texto.` | Generó la lógica de filtrado en `PostsViewModel` almacenando la lista recibida en `listaOriginal`, exponiendo el `StateFlow` de consulta e integrando el `OutlinedTextField` en `PantallaLista`. | Se estructuró la lógica de filtrado dentro de una función privada `aplicarBusqueda()` que recombina `listaOriginal` con `_consulta.value` tanto en `buscar()` como al finalizar `cargar()`. | `listaOriginal` actúa como un in-memory cache que elimina peticiones de red redundantes, ofreciendo búsqueda instantánea (0 ms de latencia). Al invocar `aplicarBusqueda()` dentro de `cargar()`, se garantiza que al cambiar el filtro de usuario la consulta de búsqueda activa se reaplique inmediatamente sobre los nuevos datos recibidos. |
| **Mejora 3: Eliminación con `@DELETE` y `Response<Unit>`**<br><br>`Implementa la eliminación de una publicación en com.galindo.murocampus respetando estrictamente MVVM + Repository: En PostApi.kt: agrega @DELETE("posts/{id}") suspend fun eliminar(@Path("id") id: Int): Response<Unit> (usando retrofit2.Response). En PostRepository.kt: agrega suspend fun eliminar(id: Int): Result<Unit> en PostRepository y en PostRepositoryRemote impleméntalo dentro de llamar { val r = api.eliminar(id); if (!r.isSuccessful) throw HttpException(r) } (aquí sí se permite HttpException porque estamos en la capa data). En PostsViewModel.kt: agrega private val _eliminado = MutableStateFlow<UiState<Unit>?>(null) y val eliminado = _eliminado.asStateFlow(), más fun eliminar(id: Int) y fun limpiarEliminado(). Prohibido usar HttpException o Response en el ViewModel. En PantallaDetalle.kt: agrega un botón rojo OutlinedButton con texto "Eliminar publicación" debajo de la tarjeta principal. Cuando _eliminado sea UiState.Exito, ejecuta vm.limpiarEliminado() y onVolver(). Si es UiState.Error, muestra el mensaje de error.` | Generó el endpoint `@DELETE` devolviendo `Response<Unit>`, el método `eliminar()` en `PostRepository` con verificación de `isSuccessful` y `HttpException`, y el manejo de estado `_eliminado` en `PostsViewModel`. | Se verificó estrictamente que el bloque `llamar` en `PostRepositoryRemote` mantuviera la captura y relanzamiento explícito de `CancellationException` antes de `Exception`, y se removieron todos los imports de `retrofit2.*` del ViewModel. | **1. Control de Cancelación:** Relanzar `CancellationException` es obligatorio en corrutinas para permitir la cancelación limpia del `Job` (ej. si el usuario sale de la pantalla).<br>**2. Encapsulamiento de Excepciones:** Ninguna excepción escala fuera del Repository; los errores HTTP (`HttpException`) se convierten a `Result.failure(e)`.<br>**3. Desacoplamiento ViewModel:** El ViewModel consume exclusivamente `Result<Unit>` y emite `UiState<Unit>`, manteniéndose 100% independiente de Retrofit/OkHttp. |

---

## Análisis Técnico Detallado para la Sustentación

### 1. Garantía de que Ninguna Excepción Escapa del Repository
En `PostRepositoryRemote`, todas las llamadas a la API REST se ejecutan dentro del método privado genérico `llamar`:

```kotlin
private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
```

- **Respuesta de Red / Errores HTTP (`HttpException`)**: Cuando una llamada HTTP falla (ej. código `404` o `500`), el código en `PostRepositoryRemote` lanza explícitamente una `HttpException(r)`. Esta es capturada por el bloque `catch (e: Exception)` y devuelta ordenadamente como `Result.failure(e)`.
- **Efecto en la Arquitectura**: El Repository jamás lanza excepciones hacia la capa de presentación. La función siempre retorna un objeto `kotlin.Result<T>`, obligando al ViewModel a manejar funcionalmente el éxito con `.onSuccess` y la falla con `.onFailure`.

### 2. Manejo Crítico de `CancellationException`
- Si la corrutina que ejecuta una operación de red es cancelada (por ejemplo, porque el usuario cierra la pantalla antes de que finalice la petición HTTP), Kotlin Coroutines lanza una `CancellationException`.
- Si el bloque `try-catch` capturara `CancellationException` dentro de `catch (e: Exception)` y devolviera un `Result.failure`, rompería el mecanismo de cancelación cooperativa de Coroutines, dejando trabajos colgados en segundo plano.
- Por esta razón, el código incluye explícitamente:
  ```kotlin
  catch (e: CancellationException) {
      throw e
  }
  ```
  Esto permite que la cancelación se propague de manera natural y limpia a través del árbol de corrutinas (`Job`).

### 3. Desacoplamiento 100% del ViewModel respecto a Retrofit y `HttpException`
- **Regla Estricta**: `PostsViewModel` **NO** importa ninguna clase de `retrofit2.*` (`HttpException`, `Response`, `Retrofit`, etc.) ni de `okhttp3.*`.
- **Flujo de Datos Unidireccional (UDF)**:
  1. El ViewModel llama a `repo.eliminar(id)`, recibiendo un `Result<Unit>`.
  2. Mediante `.onSuccess` convierte el resultado exitoso en `UiState.Exito(Unit)`.
  3. Mediante `.onFailure` convierte el error en `UiState.Error(mensajeDe(it))`, donde `mensajeDe()` extrae un mensaje legible para el usuario (`e.localizedMessage`).
- **Beneficio**: El ViewModel solo entiende de lógica de presentación y estados de UI (`UiState`). Es totalmente testeable mediante Unit Tests aislados utilizando Mocks/Fakes del repositorio sin necesidad de simular servidores HTTP ni dependencias de Retrofit.
