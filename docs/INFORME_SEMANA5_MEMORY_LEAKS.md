# Informe Semana 5 - Deteccion y Correccion de Memory Leaks

**Asignatura:** Desarrollo App Moviles II - Duoc UC
**Aplicacion:** Necesitas Ayuda?
**Herramientas:** Android Profiler, LeakCanary 2.14, Logcat

---

## 1. Que es un Memory Leak

Un **memory leak** (fuga de memoria) ocurre cuando una aplicacion mantiene referencias a objetos que ya no necesita, impidiendo que el **Garbage Collector (GC)** los libere. En Android, esto es critico porque:

- Las Activities y Fragments tienen un **ciclo de vida limitado** - se crean y destruyen frecuentemente (rotacion de pantalla, navegacion).
- Si un objeto de larga duracion (como un `companion object`, `Handler` o `GlobalScope`) mantiene una referencia a una Activity destruida, esa Activity **no puede ser recolectada por el GC**.
- Esto causa **crecimiento progresivo del heap**, lo que eventualmente lleva a un `OutOfMemoryError` y el crash de la aplicacion.

### Tipos comunes en Android

| Tipo | Causa | Impacto |
|------|-------|---------|
| Referencia estatica a Activity | `companion object { var instance = this }` | Activity nunca se libera |
| Handler sin cleanup | `Handler.postDelayed()` sin `removeCallbacks()` en `onDestroy()` | Handler mantiene referencia a Activity |
| GlobalScope en ViewModel | Coroutines que sobreviven al ViewModel | Trabajo innecesario + referencias retenidas |
| Listener no removido | Registrar listener sin des-registrar | Objeto mantiene referencia al contexto |

---

## 2. Herramientas Utilizadas

### 2.1 Android Profiler (Memory Profiler)

Herramienta integrada en Android Studio que permite:
- **Monitorear el heap en tiempo real** - visualizar el uso de memoria durante la ejecucion
- **Capturar heap dumps** - analizar que objetos estan en memoria y sus referencias
- **Forzar GC** - verificar si los objetos se liberan correctamente
- **Detectar allocations** - identificar donde se crean objetos excesivos

**Como acceder:** View > Tool Windows > Profiler > Seleccionar proceso > Memory

### 2.2 LeakCanary 2.14

Libreria open-source de Square que detecta memory leaks automaticamente:
- Se integra solo con `debugImplementation` (no afecta release)
- Monitorea Activities, Fragments, ViewModels y Views
- Genera notificaciones cuando detecta un leak
- Muestra la **cadena de retencion** completa (leak trace)

**Configuracion en el proyecto:**
```kotlin
// app/build.gradle.kts
debugImplementation(libs.leakcanary.android)
```

```toml
# gradle/libs.versions.toml
leakcanary = "2.14"
leakcanary-android = { group = "com.squareup.leakcanary", name = "leakcanary-android", version.ref = "leakcanary" }
```

LeakCanary no requiere inicializacion manual - se auto-instala via `ContentProvider`.

### 2.3 Logcat con TAG personalizado

Se utiliza el TAG `SolicitudMemory` para registrar metricas de memoria:

```
tag:SolicitudMemory    # Filtro en Logcat para memoria
```

---

## 3. Diagnostico Inicial con Android Profiler

### Escenario 1: Navegacion repetida Home -> Form -> Home

**Procedimiento:**
1. Abrir Memory Profiler
2. Navegar: HomeScreen -> FormScreen -> HomeScreen (repetir 5 veces)
3. Forzar GC despues de cada ciclo
4. Observar el comportamiento del heap

**Resultado esperado:** El heap debe mantenerse estable despues de cada GC. Si crece progresivamente, indica un memory leak.

**Observacion:** Con el codigo corregido, la memoria se mantiene estable ya que:
- El `NavController` maneja correctamente el backstack
- No hay referencias estaticas a pantallas anteriores
- El ViewModel usa `viewModelScope` que se limpia automaticamente

### Escenario 2: Rotacion de pantalla

**Procedimiento:**
1. Abrir Memory Profiler
2. Crear una solicitud en FormScreen
3. Rotar la pantalla 5 veces (Ctrl+F11 en emulador)
4. Forzar GC despues de cada rotacion
5. Verificar que la Activity anterior se libera

**Resultado esperado:** Cada rotacion destruye y recrea la Activity. Con el codigo correcto, la Activity anterior debe ser liberada por el GC.

**Observacion:** El `Handler` se limpia correctamente en `onDestroy()` con `removeCallbacks()`, permitiendo que la Activity sea recolectada.

---

## 4. Memory Leaks Detectados y Analizados

### 4.1 Leak: Referencia estatica a Activity

**Codigo CON leak (antes):**
```kotlin
class MainActivity : ComponentActivity() {
    companion object {
        // MAL: referencia estatica a Activity
        // El companion object vive durante toda la app,
        // reteniendo la Activity incluso despues de onDestroy()
        var instance: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this // LEAK: Activity retenida en companion object
        // ...
    }
    // No hay limpieza en onDestroy()
}
```

**Por que causa leak:**
- `companion object` es equivalente a `static` en Java - vive durante toda la vida de la aplicacion
- Al rotar la pantalla, `onCreate()` se llama de nuevo con la nueva Activity, pero la vieja Activity queda referenciada hasta que se asigna la nueva
- Si otro codigo accede a `MainActivity.instance` antes de la reasignacion, usa una Activity destruida

**LeakCanary reportaria:**
```
┬───
│ GC Root: Global variable in companion object
│
├─ MainActivity.Companion
│    Leaking: NO (companion objects are never garbage collected)
│    ↓ Companion.instance
├─ MainActivity
│    Leaking: YES (Activity was destroyed)
│    Activity was destroyed 15s ago
╰───
```

**Codigo CORREGIDO (despues):**
```kotlin
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val TAG_MEMORY = "SolicitudMemory"
        // BIEN: Solo constantes en companion object
        // Sin referencia estatica a Activity

        fun logMemoryUsage(context: String) {
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            val maxMemory = runtime.maxMemory() / 1024 / 1024
            Log.i(TAG_MEMORY, "Memoria [$context]: ${usedMemory}MB / ${maxMemory}MB")
        }
    }
    // ...
}
```

### 4.2 Leak: Handler sin cleanup en onDestroy

**Codigo CON leak (antes):**
```kotlin
class MainActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val memoryRunnable = object : Runnable {
        override fun run() {
            logMemoryUsage()
            handler.postDelayed(this, 10_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.postDelayed(memoryRunnable, 10_000L)
        // ...
    }

    // MAL: No hay onDestroy() con removeCallbacks()
    // El Handler sigue ejecutando el Runnable despues de
    // que la Activity se destruye, reteniendo la referencia
}
```

**Por que causa leak:**
- `Handler.postDelayed()` programa una tarea futura en el `MessageQueue` del hilo principal
- El `Runnable` es un objeto anonimo que captura una referencia implicita a la Activity (su clase externa)
- Si la Activity se destruye (ej. rotacion), el `Runnable` sigue en la cola, manteniendo viva la Activity destruida
- El Runnable se re-programa a si mismo indefinidamente

**LeakCanary reportaria:**
```
┬───
│ GC Root: Main thread message queue
│
├─ android.os.MessageQueue
│    ↓ MessageQueue.messages
├─ android.os.Message
│    ↓ Message.callback (the Runnable)
├─ MainActivity$memoryRunnable$1
│    Leaking: NO (it references the Activity)
│    ↓ anonymous class -> this$0
├─ MainActivity
│    Leaking: YES (Activity was destroyed)
╰───
```

**Codigo CORREGIDO (despues):**
```kotlin
class MainActivity : ComponentActivity() {
    private val memoryHandler = Handler(Looper.getMainLooper())
    private val memoryCheckRunnable = object : Runnable {
        override fun run() {
            logMemoryUsage("Monitoreo periodico")
            memoryHandler.postDelayed(this, MEMORY_CHECK_INTERVAL)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memoryHandler.postDelayed(memoryCheckRunnable, MEMORY_CHECK_INTERVAL)
        // ...
    }

    override fun onDestroy() {
        super.onDestroy()
        // CORRECCION: Remover callbacks pendientes del Handler
        memoryHandler.removeCallbacks(memoryCheckRunnable)
        Log.d(TAG_MEMORY, "Callbacks removidos - sin memory leak")
    }
}
```

### 4.3 Anti-patron documentado: GlobalScope vs viewModelScope

**Codigo CON problema (anti-patron):**
```kotlin
class SolicitudViewModel(application: Application) : AndroidViewModel(application) {
    fun saveSolicitud() {
        // MAL: GlobalScope no se cancela cuando el ViewModel se destruye
        GlobalScope.launch(Dispatchers.IO) {
            repository.insertSolicitud(solicitud)
            // Esta coroutine sigue ejecutandose incluso si el usuario
            // navego fuera de la pantalla y el ViewModel fue limpiado
        }
    }
}
```

**Por que es problematico:**
- `GlobalScope` crea coroutines que viven durante toda la aplicacion
- Si el ViewModel se destruye (ej. al navegar fuera), la coroutine sigue ejecutando
- Puede intentar acceder a estados ya limpiados, causando crashes
- Consume recursos innecesariamente

**Codigo CORRECTO (implementado en el proyecto):**
```kotlin
class SolicitudViewModel(application: Application) : AndroidViewModel(application) {
    fun saveSolicitud() {
        // BIEN: viewModelScope se cancela automaticamente en onCleared()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertSolicitud(solicitud)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // viewModelScope ya fue cancelado automaticamente
        Log.d(TAG_MEMORY, "ViewModel limpiado - coroutines canceladas")
    }
}
```

---

## 5. Correcciones Aplicadas

### Resumen de cambios

| Archivo | Cambio | Tipo |
|---------|--------|------|
| `MainActivity.kt` | Companion object solo con constantes (sin `instance`) | Correccion leak |
| `MainActivity.kt` | `onDestroy()` con `handler.removeCallbacks()` | Correccion leak |
| `MainActivity.kt` | Monitoreo periodico de memoria con Handler | Infraestructura |
| `MainActivity.kt` | `logMemoryUsage()` helper para metricas | Infraestructura |
| `SolicitudViewModel.kt` | `onCleared()` con logging de limpieza | Evidencia |
| `SolicitudViewModel.kt` | Uso de `viewModelScope` (ya existente) | Buena practica |
| `build.gradle.kts` | LeakCanary 2.14 como `debugImplementation` | Herramienta |

### Principios aplicados

1. **No guardar referencias estaticas a Activity** - El `companion object` solo contiene constantes y funciones puras
2. **Limpiar recursos en onDestroy()** - `Handler.removeCallbacks()` previene el leak
3. **Usar viewModelScope** - Se cancela automaticamente cuando el ViewModel se limpia
4. **debugImplementation para LeakCanary** - Solo se incluye en builds de debug, sin impacto en produccion

---

## 6. Validacion Posterior

### Verificacion con Android Profiler

Despues de aplicar las correcciones:

1. **Navegacion repetida (5 ciclos Home -> Form -> Home):**
   - Heap se mantiene estable despues de GC
   - No hay crecimiento progresivo de memoria
   - Activities anteriores son recolectadas correctamente

2. **Rotacion de pantalla (5 rotaciones):**
   - Cada Activity destruida es liberada por GC
   - Handler no retiene la Activity (callbacks removidos en onDestroy)
   - Memoria se estabiliza rapidamente

### Verificacion con LeakCanary

- LeakCanary no reporta leaks despues de las correcciones
- Todas las Activities son recolectadas correctamente
- El icono de LeakCanary en la barra de notificaciones muestra 0 leaks

### Verificacion con Logcat

Filtrar con `tag:SolicitudMemory` muestra:
```
I/SolicitudMemory: ═══ Memoria [onCreate] ═══
I/SolicitudMemory:   Usada: 45MB / 256MB (17%)
I/SolicitudMemory:   Libre: 12MB | Total asignada: 57MB
D/SolicitudMemory: Monitoreo de memoria iniciado (cada 10s)
I/SolicitudMemory: ═══ Memoria [Monitoreo periodico] ═══
I/SolicitudMemory:   Usada: 47MB / 256MB (18%)
...
D/SolicitudMemory: Monitoreo de memoria detenido - callbacks removidos
I/SolicitudMemory: ═══ Memoria [onDestroy] ═══
I/SolicitudMemory:   Usada: 46MB / 256MB (17%)
D/SolicitudMemory: ViewModel onCleared - viewModelScope cancelado automaticamente
```

---

## 7. Capturas de Pantalla

Las capturas se encuentran en `docs/screenshots/memory_leaks/`:

| Captura | Descripcion |
|---------|-------------|
| `profiler_memoria_antes.png` | Memory Profiler antes de correcciones |
| `profiler_memoria_despues.png` | Memory Profiler despues de correcciones (heap estable) |
| `leakcanary_no_leaks.png` | LeakCanary mostrando 0 leaks |
| `logcat_memoria.png` | Logcat filtrado con `tag:SolicitudMemory` |

---

## 8. Conclusiones

1. Los **memory leaks mas comunes** en Android son causados por referencias estaticas a Activities y Handlers sin cleanup
2. **LeakCanary** es una herramienta esencial para detectar leaks automaticamente durante el desarrollo
3. **Android Profiler** permite verificar visualmente que la memoria se libera correctamente
4. Usar **viewModelScope** en lugar de GlobalScope garantiza que las coroutines se cancelen con el ViewModel
5. El **principio fundamental** es: todo recurso que se adquiere en `onCreate()` debe liberarse en `onDestroy()`
