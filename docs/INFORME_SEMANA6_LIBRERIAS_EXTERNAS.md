# Informe Tecnico - Semana 6
# Potenciando la App con Librerias Externas

**Asignatura:** Desarrollo APP Moviles II (PMY2202)
**Estudiante:** Franco Castro Villanueva
**Institucion:** Duoc UC
**Fecha:** 16 de febrero de 2026
**Aplicacion:** Necesitas Ayuda?

---

## 1. Flujo Funcional Seleccionado

### 1.1 Descripcion del Flujo

Se selecciono el flujo **"Consulta de Tecnicos Disponibles"** como flujo critico para aplicar las mejoras avanzadas de la Semana 6. Este flujo comprende la siguiente secuencia:

```
DetailScreen -> [Boton "Ver Tecnicos Disponibles"] -> TecnicosScreen
                                                          |
                                                          v
                                                   API Retrofit (MockInterceptor)
                                                          |
                                                          v
                                                   Loading -> Success/Error
                                                          |
                                                          v
                                                   LazyColumn con TecnicoCard
```

El usuario accede al detalle de una solicitud existente (almacenada en Room Database), presiona el boton "Ver Tecnicos Disponibles", y la aplicacion realiza una consulta API mediante Retrofit para obtener una lista de tecnicos especializados segun el tipo de servicio (gasfiteria, electricidad o electrodomesticos).

### 1.2 Importancia del Flujo

Este flujo es critico porque permite aplicar **multiples tecnicas avanzadas** en un solo recorrido:

| Tecnica | Aplicacion en el flujo |
|---------|----------------------|
| Persistencia local (Room) | La solicitud se carga desde la base de datos local |
| Comunicacion de red (Retrofit) | Se consulta la API para obtener tecnicos |
| Procesos asincronos (Coroutines) | La llamada API se ejecuta en Dispatchers.IO |
| Manejo de errores (try-catch) | Se capturan HttpException, IOException y Exception |
| Estados reactivos (StateFlow) | NetworkResult sealed class maneja Loading/Success/Error |
| Libreria externa (Retrofit+OkHttp+Gson) | Toda la capa de red utiliza estas librerias |
| Accesibilidad | TecnicosScreen cumple con contentDescription, heading(), 56dp buttons |

Ademas, el flujo conecta la funcionalidad existente de la app (gestion de solicitudes) con una nueva funcionalidad (directorio de tecnicos), demostrando como una libreria externa puede extender las capacidades del sistema de manera coherente con la arquitectura MVVM existente.

---

## 2. Mejoras Implementadas

### 2.1 Integracion de Libreria Externa: Retrofit 2 + OkHttp + Gson

Se integraron tres librerias externas que trabajan en conjunto para la comunicacion API REST:

**Dependencias agregadas en `gradle/libs.versions.toml` y `app/build.gradle.kts`:**

| Libreria | Version | Artefacto |
|----------|---------|-----------|
| Retrofit | 2.9.0 | `com.squareup.retrofit2:retrofit` |
| Retrofit Gson Converter | 2.9.0 | `com.squareup.retrofit2:converter-gson` |
| OkHttp | 4.12.0 | `com.squareup.okhttp3:okhttp` |
| OkHttp Logging Interceptor | 4.12.0 | `com.squareup.okhttp3:logging-interceptor` |
| Gson | 2.10.1 | `com.google.code.gson:gson` |

**Archivos creados para la capa de red:**

| Archivo | Descripcion |
|---------|-------------|
| `ApiService.kt` | Interface Retrofit con endpoint `@GET("tecnicos/{tipo}")` |
| `RetrofitClient.kt` | Singleton que configura OkHttpClient con interceptores y timeouts de 10 segundos |
| `MockInterceptor.kt` | Interceptor OkHttp que simula respuestas API con datos realistas de tecnicos chilenos |
| `NetworkResult.kt` | Sealed class con estados `Loading`, `Success<T>` y `Error(message)` |
| `TecnicoDto.kt` | Data class con `@SerializedName` para mapeo JSON automatico |
| `TecnicosResponse.kt` | Wrapper de respuesta con campos success, data, count, timestamp |
| `TecnicoRepository.kt` | Repository que ejecuta la llamada API en Dispatchers.IO |
| `TecnicoViewModel.kt` | ViewModel con StateFlow para estado reactivo de la UI |
| `TecnicosScreen.kt` | Pantalla Compose con estados Loading, Success y Error |

**Evidencia - TecnicosScreen mostrando tecnicos de gasfiteria:**

![Tecnicos Gasfiteria](screenshots/semana6/tecnicos_gasfiteria.png)

**Evidencia - TecnicosScreen mostrando tecnicos de electricidad:**

![Tecnicos Electricidad](screenshots/semana6/tecnicos_electricidad.png)

### 2.2 Procesos Asincronos (Kotlin Coroutines)

Todas las operaciones de red se ejecutan de forma asincrona utilizando Kotlin Coroutines, garantizando que la UI nunca se bloquee durante las llamadas API.

**Implementacion en TecnicoRepository.kt:**

```kotlin
suspend fun getTecnicosByTipo(tipo: String): NetworkResult<List<TecnicoDto>> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTecnicosByTipo(tipo)
            if (response.success) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error("El servidor no pudo procesar la solicitud")
            }
        } catch (e: HttpException) {
            NetworkResult.Error("Error del servidor: ${e.code()}")
        } catch (e: IOException) {
            NetworkResult.Error("Error de conexion: Verifique su internet")
        } catch (e: Exception) {
            NetworkResult.Error("Error inesperado: ${e.localizedMessage}")
        }
    }
}
```

**Implementacion en TecnicoViewModel.kt:**

```kotlin
fun loadTecnicos(tipoServicio: String) {
    _tecnicosState.value = NetworkResult.Loading

    viewModelScope.launch {
        val result = repository.getTecnicosByTipo(tipoServicio)
        _tecnicosState.value = result
    }
}
```

**Caracteristicas clave de la implementacion asincrona:**

| Caracteristica | Implementacion |
|----------------|----------------|
| Dispatcher de red | `Dispatchers.IO` para operaciones de red en hilo secundario |
| Scope del ViewModel | `viewModelScope.launch` para cancelacion automatica |
| Estado de carga | `NetworkResult.Loading` muestra `CircularProgressIndicator` |
| Feedback de error | `NetworkResult.Error` muestra mensaje y boton "Reintentar" |
| Simulacion de latencia | `MockInterceptor` con delay aleatorio de 100-300ms |

El `MockInterceptor` simula condiciones reales de red con un delay de 100-300ms, permitiendo verificar que el estado de carga (`CircularProgressIndicator`) se muestra correctamente antes de que los datos aparezcan en pantalla.

### 2.3 Debugging y Manejo de Errores

Se implemento un sistema completo de debugging con logging estructurado y manejo de excepciones especificas en toda la capa de red.

**TAGs de Logcat implementados:**

| TAG | Archivo | Proposito |
|-----|---------|-----------|
| `SolicitudAPI` | TecnicoRepository.kt | Logs de requests y responses API |
| `MockInterceptor` | MockInterceptor.kt | Interceptor simulando respuestas y delays |
| `RetrofitClient` | RetrofitClient.kt | Configuracion del cliente HTTP y body de requests |
| `SolicitudPerformance` | TecnicoRepository.kt, TecnicoViewModel.kt | Tiempos de ejecucion en milisegundos |
| `SolicitudMemory` | TecnicoViewModel.kt | Uso de memoria al iniciar y limpiar ViewModel |

**Excepciones capturadas en TecnicoRepository:**

| Excepcion | Causa | Mensaje al usuario |
|-----------|-------|-------------------|
| `HttpException` | Error de servidor (4xx, 5xx) | "Error del servidor: {codigo}" |
| `IOException` | Sin conexion a internet | "Error de conexion: Verifique su internet" |
| `Exception` | Error inesperado | "Error inesperado: {mensaje}" |

**Evidencia - Logcat filtrando tag:SolicitudAPI:**

Los logs muestran el ciclo completo de la llamada API: inicio de la consulta, ejecucion en Dispatchers.IO, respuesta exitosa con cantidad de tecnicos encontrados y nombres recibidos.

![Logcat SolicitudAPI](screenshots/semana6/logcat_solicitud_api.png)

**Evidencia - Logcat filtrando tag:MockInterceptor:**

Los logs muestran el interceptor en accion: interceptando cada request, simulando el delay de red (100-300ms) y respondiendo con datos mock segun la ruta solicitada.

![Logcat MockInterceptor](screenshots/semana6/logcat_mock_interceptor.png)

**Evidencia - Logcat filtrando tag:SolicitudPerformance:**

Los logs muestran metricas de rendimiento: tiempo total de la API call, tiempo de carga de detalles, y tiempos de operaciones CRUD, permitiendo identificar cuellos de botella.

![Logcat Performance](screenshots/semana6/logcat_performance.png)

### 2.4 Diagnostico y Prevencion de Memory Leaks con LeakCanary

Se utilizo **LeakCanary 2.14** para diagnosticar memory leaks en la aplicacion. Se creo el archivo `MemoryLeakDemo.kt` con tres ejemplos educativos de leaks comunes en Android.

#### Ejemplo de Leak Reproducido: Referencia Estatica a Activity

**Problema:** Se almacenaron referencias a Activities en una lista estatica (`companion object`). Cada vez que el Activity se destruye por rotacion de pantalla, la referencia queda en la lista impidiendo que el garbage collector libere la memoria.

**Codigo que provoca el leak:**

```kotlin
object LeakExample1_StaticActivity {
    // MAL: Lista estatica que acumula Activities destruidos
    private val leakedActivities = mutableListOf<Activity>()

    fun simulateLeak(activity: Activity) {
        leakedActivities.add(activity)
        // Cada rotacion agrega el Activity a la lista.
        // Los Activities destruidos NO pueden ser liberados por el GC.
    }
}
```

**Pasos para reproducir:**
1. Se agrego `MemoryLeakDemo.LeakExample1_StaticActivity.simulateLeak(this)` en `MainActivity.onCreate()`
2. Se ejecuto la app en el emulador
3. Se roto el dispositivo 6-8 veces para acumular Activities destruidos
4. Se abrio la app "Leaks" (instalada por LeakCanary) y se forzó el heap dump
5. LeakCanary detecto **1 APPLICATION LEAK** con 1.401.528 bytes retenidos

**Evidencia - LeakCanary detectando el memory leak:**

El reporte muestra la cadena de referencias: `MemoryLeakDemo$LeakExample1_StaticActivity` -> `leakedActivities` -> `MainActivity instance (Leaking: YES)`.

![LeakCanary Leak Detectado](screenshots/semana6/leakcanary_leak_detectado.png)

**Correccion aplicada:**

```kotlin
object LeakExample1_StaticActivity {
    // BIEN: WeakReference permite que el GC libere el Activity
    private var safeActivityRef: WeakReference<Activity>? = null

    fun safeStore(activity: Activity) {
        safeActivityRef = WeakReference(activity)
        // Si el Activity se destruye, el GC puede liberarlo
        // y safeActivityRef.get() retornara null
    }
}
```

Se reemplazo la referencia directa por `WeakReference`, que permite al garbage collector liberar el Activity cuando ya no hay referencias fuertes. Tras aplicar la correccion, se repitio el proceso de rotacion y heap dump.

**Evidencia - LeakCanary confirmando 0 leaks tras la correccion:**

![LeakCanary Leak Corregido](screenshots/semana6/leakcanary_leak_corregido.png)

#### Otros Ejemplos de Memory Leaks Documentados

El archivo `MemoryLeakDemo.kt` incluye dos ejemplos adicionales con sus correcciones:

| Leak | Causa | Correccion |
|------|-------|------------|
| **Handler sin cleanup** | `postDelayed` con callback de 30 segundos mantiene referencia al Activity | Llamar `handler.removeCallbacksAndMessages(null)` en `onDestroy()` |
| **Listener sin unregister** | Listeners registrados en singleton nunca se remueven, acumulandose | Implementar `register()` en `onCreate()` y `unregister()` en `onDestroy()` |

La aplicacion ya implementa la prevencion del leak de Handler en `MainActivity.kt`:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    memoryHandler.removeCallbacks(memoryCheckRunnable)  // Previene leak
}
```

---

## 3. Justificacion Tecnica

### 3.1 Retrofit 2.9.0

Retrofit es la libreria estandar de la industria para comunicacion API REST en Android. Se eligio por las siguientes razones:

- **Declarativa**: Los endpoints se definen como interfaces Kotlin con anotaciones (`@GET`, `@POST`, `@Path`), lo que hace el codigo limpio y facil de mantener
- **Soporte nativo de Coroutines**: Las funciones `suspend` permiten integrar llamadas API directamente con el sistema de coroutines de Kotlin
- **Extensible**: Soporta multiples convertidores (Gson, Moshi, Jackson) e interceptores
- **Probada en produccion**: Utilizada por miles de aplicaciones Android en produccion

### 3.2 OkHttp 4.12.0

Se eligio OkHttp como cliente HTTP por:

- **Interceptores**: Permiten implementar el `MockInterceptor` para simular APIs sin servidor y el `HttpLoggingInterceptor` para debugging
- **Configuracion de timeouts**: Connect, read y write timeouts configurables (10s en nuestra app)
- **Eficiencia**: Connection pooling, compresion GZIP automatica y cache HTTP
- **Complemento natural de Retrofit**: Retrofit utiliza OkHttp internamente

### 3.3 Gson 2.10.1

Se eligio Gson para la serializacion JSON por:

- **Automatica**: Convierte JSON a data classes Kotlin con `@SerializedName` sin parseo manual
- **Integrada con Retrofit**: `GsonConverterFactory` se conecta directamente al builder de Retrofit
- **Libreria de Google**: Ampliamente documentada y mantenida

### 3.4 Kotlin Coroutines

Se utilizaron Coroutines (ya integradas desde Semana 3) para la capa de red:

- **Dispatchers.IO**: Ejecuta operaciones de red en un pool de hilos optimizado para I/O
- **viewModelScope**: Cancelacion automatica al destruirse el ViewModel, previniendo leaks
- **Funciones suspend**: Permiten escribir codigo asincrono de forma secuencial y legible

### 3.5 LeakCanary 2.14

Se utilizo LeakCanary para diagnostico de memory leaks:

- **Automatica**: Detecta leaks sin configuracion adicional en builds debug
- **Heap analysis**: Analiza el heap dump e identifica cadenas de referencias
- **No afecta produccion**: Solo se incluye como `debugImplementation`, no esta presente en builds release

---

## 4. Reflexion sobre el Impacto

### 4.1 Impacto en la Calidad

La integracion de Retrofit con manejo de errores estructurado (try-catch con excepciones especificas) mejora significativamente la robustez de la aplicacion. Antes de la Semana 6, la app solo operaba con datos locales y cualquier fallo de Room se manejaba de forma generica. Ahora, la capa de red distingue entre errores de servidor (`HttpException`), errores de conexion (`IOException`) y errores inesperados, proporcionando mensajes informativos al usuario en cada caso.

El sistema de logging con TAGs especificos (`SolicitudAPI`, `MockInterceptor`, `SolicitudPerformance`) permite diagnosticar problemas rapidamente durante el desarrollo. Las metricas de rendimiento en milisegundos facilitan la identificacion de cuellos de botella antes de que afecten a los usuarios.

El diagnostico de memory leaks con LeakCanary agrega una capa adicional de calidad. La deteccion temprana de leaks (como la referencia estatica a Activity) previene problemas de rendimiento y crashes por `OutOfMemoryError` que serian dificiles de diagnosticar en produccion.

### 4.2 Impacto en la Escalabilidad

La arquitectura implementada facilita la transicion de una API simulada a una API real. El `MockInterceptor` puede reemplazarse simplemente removiendolo del `OkHttpClient.Builder()` en `RetrofitClient.kt`, sin necesidad de modificar `ApiService`, `TecnicoRepository`, `TecnicoViewModel` ni `TecnicosScreen`. Esta separacion de responsabilidades es fundamental para la escalabilidad.

El patron Repository (`TecnicoRepository`) abstrae la fuente de datos, permitiendo agregar cache local con Room en el futuro sin modificar el ViewModel ni la UI. La `NetworkResult` sealed class es reutilizable para cualquier nuevo endpoint que se agregue.

La estructura MVVM con capas separadas (`data/remote/`, `data/repository/`, `viewmodel/`, `ui/screens/`) permite que multiples desarrolladores trabajen en diferentes capas simultaneamente sin conflictos.

### 4.3 Impacto en la Experiencia de Usuario

El estado `Loading` con `CircularProgressIndicator` proporciona feedback visual inmediato al usuario, indicando que la aplicacion esta procesando su solicitud. Sin esta mejora, el usuario veria una pantalla en blanco durante la carga, generando confusion.

El estado `Error` con boton "Reintentar" permite al usuario recuperarse de fallos de red sin necesidad de navegar hacia atras y volver a intentar. Este patron de "retry" es estandar en aplicaciones modernas y mejora significativamente la experiencia ante problemas de conectividad.

La accesibilidad en `TecnicosScreen` (contentDescription en cards, heading() en titulos, botones de 56dp) garantiza que todos los usuarios, incluyendo aquellos con discapacidades visuales que utilizan TalkBack, puedan navegar e interactuar con el directorio de tecnicos.

---

## 5. Referencias a Capturas en GitHub

Todas las capturas de pantalla se encuentran en el repositorio de GitHub en la carpeta `docs/screenshots/semana6/`:

| Captura | Archivo | Evidencia |
|---------|---------|-----------|
| TecnicosScreen - Gasfiteria | `tecnicos_gasfiteria.png` | Libreria externa funcionando, datos mock cargados |
| TecnicosScreen - Electricidad | `tecnicos_electricidad.png` | Libreria externa con otro tipo de servicio |
| Logcat tag:SolicitudAPI | `logcat_solicitud_api.png` | Debugging, logging de requests API |
| Logcat tag:MockInterceptor | `logcat_mock_interceptor.png` | Interceptor OkHttp simulando respuestas |
| Logcat tag:SolicitudPerformance | `logcat_performance.png` | Metricas de rendimiento en milisegundos |
| LeakCanary - Leak detectado | `leakcanary_leak_detectado.png` | 1 APPLICATION LEAK con heap trace |
| LeakCanary - Leak corregido | `leakcanary_leak_corregido.png` | 0 APPLICATION LEAKS tras correccion |

---

## 6. Conclusion

La Semana 6 represento un avance significativo en la madurez tecnica de la aplicacion "Necesitas Ayuda?". La integracion de Retrofit 2, OkHttp y Gson como librerias externas permitio extender la funcionalidad del sistema desde una aplicacion puramente local a una con capacidad de comunicacion de red, siguiendo las mejores practicas de la industria Android.

La combinacion de procesos asincronos con Coroutines, manejo de errores estructurado, logging detallado y diagnostico de memory leaks con LeakCanary conforman un sistema robusto, escalable y mantenible. La separacion de responsabilidades en la arquitectura MVVM facilita futuras extensiones como la conexion a un backend real, cache local de tecnicos, o la adicion de nuevos endpoints.

---

*Documento generado como parte de la actividad sumativa Semana 6 - Desarrollo APP Moviles II (PMY2202) - Duoc UC*
