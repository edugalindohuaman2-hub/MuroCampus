# MuroCampus

Aplicación Android nativa desarrollada en Kotlin y Jetpack Compose que consume la API REST de JSONPlaceholder mediante Retrofit, OkHttp y kotlinx.serialization bajo la arquitectura MVVM + Repository.

---

## Estructura de Paquetes

```text
com.galindo.murocampus
├── MainActivity.kt
├── data
│   ├── PostRepository.kt
│   └── remote
│       ├── PostApi.kt
│       └── RetrofitClient.kt
├── model
│   └── Modelos.kt
├── ui
│   ├── Componentes.kt
│   ├── PantallaCrear.kt
│   ├── PantallaDetalle.kt
│   ├── PantallaLista.kt
│   └── UiState.kt
└── viewmodel
    ├── PostsViewModel.kt
    └── PostsViewModelFactory.kt
```

---

## Arquitectura MVVM + Repository

El proyecto implementa el patrón MVVM combinado con el patrón Repository. El flujo de datos es estrictamente unidireccional (UDF - Unidirectional Data Flow):

```text
┌────────────────────────────────────────────────────────┐
│                        UI                              │
│ (PantallaLista, PantallaDetalle, PantallaCrear)        │
└──────────────────────────┬─────────────────────────────┘
                           │ Observa StateFlow<UiState>
                           ▼
┌────────────────────────────────────────────────────────┐
│                    ViewModel                           │
│                 (PostsViewModel)                       │
└──────────────────────────┬─────────────────────────────┘
                           │ Invoca suspend fun -> Result<T>
                           ▼
┌────────────────────────────────────────────────────────┐
│                   Repository                           │
│              (PostRepositoryRemote)                    │
└──────────────────────────┬─────────────────────────────┘
                           │ Llama API remota
                           ▼
┌────────────────────────────────────────────────────────┐
│             Retrofit / OkHttp Client                   │
│                     (PostApi)                          │
└────────────────────────────────────────────────────────┘
```

---

## Capturas de Pantalla

| Pantalla | Descripción | Imagen |
|---|---|---|
| **Lista** | Listado general de publicaciones | ![Lista](capturas/lista.png) |
| **Filtro** | Filtrado por usuario mediante chips | ![Filtro](capturas/filtro.png) |
| **Detalle** | Publicación completa con sus comentarios | ![Detalle](capturas/detalle.png) |
| **Crear** | Formulario de creación de nueva publicación | ![Crear](capturas/crear.png) |
| **Error** | Estado de error con opción de reintento | ![Error](capturas/error.png) |

---

## Para Pensar

### 1. ¿Qué diferencia hay entre GET y POST? ¿Por qué `crear` lleva `@Body` y `porUsuario` lleva `@Query`?
- **GET vs. POST**: `GET` se utiliza para obtener datos del servidor de manera idempotente y segura sin alterar su estado interno. `POST` se utiliza para enviar información al servidor con el fin de crear un nuevo recurso o procesar datos que modifican el estado en el servidor.
- **`@Body` vs. `@Query`**:
  - `crear` lleva `@Body` porque la información del objeto `NuevoPost` debe serializarse en formato JSON dentro del cuerpo de la petición HTTP (`HTTP Request Body`).
  - `porUsuario` lleva `@Query` porque el filtro `userId` se adjunta como un parámetro en la URL de consulta (ej. `posts?userId=1`), adecuado para búsquedas y filtrados en operaciones `GET`.

### 2. ¿Por qué la ruta es `"posts"` y no `"/posts"`?
En Retrofit, si la ruta del `@GET` o `@POST` comienza con una barra diagonal `/` (ej. `"/posts"`), se sobreescribe completamente la ruta relativa de la `baseUrl` configurada en `Retrofit.Builder` (ej. `https://jsonplaceholder.typicode.com/`), ignorando cualquier subruta previa. Definir la ruta relativa sin barra inicial (`"posts"`) garantiza que la URL final se resuelva concatenando correctamente la `baseUrl` terminada en `/` con la ruta relativa del endpoint (`https://jsonplaceholder.typicode.com/posts`).

### 3. ¿Por qué el Repository devuelve `Result` en vez de dejar que la excepción llegue al ViewModel?
El Repository es el encargado de abstraer la fuente de datos y manejar los fallos de red o infraestructura. Al encapsular la respuesta en `kotlin.Result` (`Result.success` o `Result.failure`), se evita que las excepciones no controladas escalen y rompan el flujo de ejecución en las corrutinas del ViewModel. Además, obliga a la capa ViewModel a manejar explícitamente tanto el éxito como el fracaso mediante funciones funcionales idiomáticas como `.onSuccess` y `.onFailure`.

### 4. ¿Qué pasa si olvidas el permiso `INTERNET`? ¿Y si olvidas `ignoreUnknownKeys` y el servidor agrega un campo nuevo?
- **Sin permiso `INTERNET`**: La aplicación lanzará una excepción `SecurityException` al intentar abrir una conexión de red con Retrofit/OkHttp, resultando en una falla de red inmediata o el cierre inesperado (crash) de la app si no se captura.
- **Sin `ignoreUnknownKeys = true`**: El deserializador de `kotlinx.serialization` lanzará una excepción `UnknownKeyException` al encontrar cualquier propiedad en el JSON proveniente del servidor que no esté explícitamente declarada en la data class Kotlin, rompiendo la compatibilidad ante cambios o evoluciones de la API REST.

### 5. ¿Por qué la pantalla de lista usa un `when` sobre `UiState` y no un `if` con tres variables?
El uso de una `sealed interface` (`UiState`) garantiza el patrón de **Estados Imposibles Inexistentes** (Illegal States Unrepresentable). Con tres variables booleanas independientes (`cargando`, `error`, `datos`), podrían darse combinaciones inconsistentes en la UI como estar cargando y mostrando error a la vez. Mediante `when` sobre `UiState`, el compilador de Kotlin verifica que los estados sean exhaustivos y mutuamente excluyentes, asegurando que la UI solo renderice exactamente uno de los estados posibles (`Cargando`, `Exito` o `Error`).

### 6. JSONPlaceholder no guarda los POST. ¿Cómo cambiaría tu app si usaras un servicio real?
1. **Actualización de UI / Estado local**: Al crear una publicación exitosamente con un servicio real, la nueva publicación persistida se retornaría desde el servidor. Podríamos actualizar inmediatamente la lista local en el `PostsViewModel` agregando el nuevo elemento retornado al estado, o volver a ejecutar `cargar()` para refrescar los datos desde la API.
2. **Navegación automática**: Tras una creación exitosa (`UiState.Exito`), la aplicación redirigiría automáticamente al usuario de vuelta a la lista o a la pantalla de detalle del nuevo elemento creado.
3. **Persistencia y Caché local**: En una app de producción se implementaría una capa de almacenamiento local (como Room o DataStore) como "Fuente Única de Verdad" (Single Source of Truth), sincronizando la base de datos local con el backend REST mediante un repositorio reactivo.
