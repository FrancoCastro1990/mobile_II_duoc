# Necesitas Ayuda?

Aplicacion Android desarrollada con Jetpack Compose para la startup "Necesitas Ayuda?" que conecta personas con prestadores de servicios a domicilio.

## Descripcion

Esta aplicacion permite gestionar solicitudes de servicios a domicilio en tres categorias:
- **Gasfiteria**: Reparacion de canerias, llaves, filtraciones
- **Electricidad**: Instalaciones y reparaciones electricas
- **Electrodomesticos**: Reparacion de refrigeradores, lavadoras, etc.

Desarrollada como actividad formativa para el curso "Desarrollo App Moviles II" de Duoc UC.

## Capturas de Pantalla

La aplicacion consta de 3 pantallas principales:

1. **HomeScreen** - Lista de solicitudes con estado
2. **FormScreen** - Formulario para crear/editar solicitudes
3. **DetailScreen** - Detalle de solicitud con opciones de gestion

## Requisitos Tecnicos

- Android Studio Hedgehog (2023.1.1) o superior
- Kotlin 2.0.0
- Jetpack Compose BOM 2024.09.00
- MinSDK: 25 (Android 7.1)
- TargetSDK: 35 (Android 15)

## Arquitectura

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt                 # Activity principal
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room Database singleton
│   │   ├── SolicitudDao.kt         # Data Access Object
│   │   └── SolicitudEntity.kt      # Entidad de base de datos
│   └── repository/
│       └── SolicitudRepository.kt  # Repositorio de datos
├── viewmodel/
│   └── SolicitudViewModel.kt       # ViewModel con StateFlow
├── navigation/
│   └── NavGraph.kt                 # Configuracion de navegacion
└── ui/
    ├── components/
    │   └── SolicitudCard.kt        # Card reutilizable
    ├── screens/
    │   ├── HomeScreen.kt           # Lista de solicitudes
    │   ├── FormScreen.kt           # Formulario CRUD
    │   └── DetailScreen.kt         # Detalle con BottomSheet
    └── theme/
        ├── Color.kt                # Colores WCAG AA
        ├── Type.kt                 # Tipografia min 14sp
        └── Theme.kt                # Tema light/dark
```

## Tecnologias y Dependencias

| Tecnologia | Version | Proposito |
|------------|---------|-----------|
| Room Database | 2.6.1 | Persistencia local |
| Navigation Compose | 2.7.7 | Navegacion entre pantallas |
| Material 3 | BOM 2024.09.00 | Componentes de UI |
| Lifecycle ViewModel | 2.6.1 | Gestion de estado |
| Kotlin Coroutines | 1.7.3 | Operaciones asincronas |
| KSP | 2.0.0-1.0.21 | Procesador de anotaciones |

## Componentes UI Avanzados

| Componente | Ubicacion | Uso |
|------------|-----------|-----|
| LazyColumn | HomeScreen | Lista scrollable de solicitudes |
| AlertDialog | FormScreen, DetailScreen | Seleccion de servicio, confirmacion |
| ModalBottomSheet | DetailScreen | Cambio de estado |
| CircularProgressIndicator | FormScreen | Indicador de carga durante guardado |
| Toast | FormScreen, DetailScreen | Feedback de acciones |

## Optimizacion con Coroutines (Semana 3)

### Mejoras Implementadas

La aplicacion implementa Kotlin Coroutines para optimizar las operaciones asincronas:

| Caracteristica | Implementacion |
|----------------|----------------|
| Estados de carga | `isSaving`, `isDeleting` con StateFlow |
| Dispatcher explicito | `withContext(Dispatchers.IO)` para operaciones de BD |
| Prevencion de duplicados | Verificacion de estado antes de ejecutar |
| Manejo de errores | try-catch con feedback al usuario |

### Flujo Optimizado

```kotlin
// Ejemplo de operacion optimizada
viewModelScope.launch {
    _isSaving.value = true
    try {
        withContext(Dispatchers.IO) {
            repository.insertSolicitud(solicitud)
        }
        _uiEvent.emit(UiEvent.ShowToast("Guardado correctamente"))
    } catch (e: Exception) {
        _uiEvent.emit(UiEvent.ShowToast("Error: ${e.localizedMessage}"))
    } finally {
        _isSaving.value = false
    }
}
```

### Beneficios

- **UI responsiva**: La interfaz nunca se bloquea durante operaciones
- **Feedback visual**: Indicador de progreso durante guardado
- **Prevencion de errores**: Boton deshabilitado durante operaciones
- **Robustez**: Manejo gracioso de errores con mensajes informativos

## Debugging y Optimizacion (Semana 4)

### Logging con Logcat

Se implemento un sistema de logging estructurado con TAGs personalizados:

| TAG | Proposito |
|-----|-----------|
| `SolicitudViewModel` | Logs generales del ViewModel |
| `SolicitudCRUD` | Operaciones Create, Read, Update, Delete |
| `SolicitudPerformance` | Metricas de tiempo de ejecucion |
| `SolicitudValidation` | Validacion de datos del formulario |
| `SolicitudDB` | Operaciones en base de datos |
| `SolicitudRepository` | Capa Repository |

### Manejo de Excepciones

Se implementaron try-catch con excepciones especificas:

| Excepcion | Causa |
|-----------|-------|
| `SQLiteException` | Errores de base de datos |
| `IllegalStateException` | Estados invalidos |
| `IOException` | Errores de almacenamiento |
| `IllegalArgumentException` | Argumentos invalidos |

### Analisis de Rendimiento

- **CPU Profiler**: Analisis de consumo de CPU durante operaciones
- **Memory Profiler**: Heap Dump con 0 memory leaks detectados
- **Metricas de tiempo**: Medicion en ms de todas las operaciones CRUD

## Cumplimiento de Rubrica

### Semana 2 - Persistencia y UI

| Criterio | Pts | Estado |
|----------|-----|--------|
| Persistencia Local (Room) | 30 | Implementado - Entity, DAO, Repository |
| Interfaz Moderna | 25 | Implementado - LazyColumn, ModalBottomSheet, AlertDialog |
| Accesibilidad | 20 | Implementado - 14sp min, contentDescription, semantics |
| Organizacion MVVM | 15 | Implementado - data/, viewmodel/, ui/screens/ |
| Documentacion | 10 | Implementado - README.md completo |

### Semana 3 - Coroutines

| Criterio | Estado |
|----------|--------|
| Flujo critico identificado | Guardar/Editar solicitudes |
| Dispatchers.IO implementado | En todas las operaciones de BD |
| Estados de carga (StateFlow) | isSaving, isDeleting |
| Manejo de errores (try-catch) | Con feedback al usuario |
| Prevencion de operaciones duplicadas | Verificacion de estado |

### Semana 4 - Debugging y Optimizacion

| Criterio | Estado |
|----------|--------|
| Flujo critico seleccionado | `saveSolicitud()` con justificacion |
| Logcat con filtros y TAGs | 6 TAGs implementados |
| Try-catch estrategico | 4 excepciones especificas |
| Herramientas de Profiling | CPU Profiler + Memory Profiler |
| Informe tecnico | Documentado en `docs/` |

## Accesibilidad (WCAG AA)

La aplicacion cumple con estandares de accesibilidad:

- Tipografia minima de 14sp en todos los textos
- `contentDescription` en todos los iconos, botones e imagenes
- Alto contraste en colores:
  - Primary: #1565C0 (Azul)
  - Secondary: #2E7D32 (Verde)
  - Error: #C62828 (Rojo)
- Botones con altura minima de 56dp
- Encabezados semanticos con `semantics { heading() }`
- Soporte para TalkBack

## Modelo de Datos

### SolicitudEntity

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| id | Long | ID autogenerado |
| tipoServicio | String | gasfiteria/electricidad/electrodomesticos |
| descripcion | String | Descripcion del problema |
| nombreCliente | String | Nombre del cliente |
| telefono | String | Telefono de contacto |
| direccion | String | Direccion del servicio |
| fechaSolicitud | Long | Timestamp de creacion |
| estado | String | pendiente/en_proceso/completado |

## Estados de Solicitud

| Estado | Color | Descripcion |
|--------|-------|-------------|
| Pendiente | Naranja | Esperando asignacion |
| En Proceso | Azul | Tecnico asignado |
| Completado | Verde | Servicio finalizado |

## Instalacion

1. Clonar el repositorio
2. Abrir en Android Studio
3. Sincronizar Gradle
4. Ejecutar en emulador o dispositivo fisico

```bash
# Compilar el proyecto
./gradlew assembleDebug

# Ejecutar tests
./gradlew test

# Generar APK
./gradlew assembleDebug
# APK ubicado en: app/build/outputs/apk/debug/app-debug.apk
```

## Flujo de Usuario

```
HomeScreen (Lista vacia)
    |
    v
[FAB +] --> FormScreen (Nueva solicitud)
    |           |
    |           v
    |       [Guardar] --> CircularProgressIndicator --> Toast
    |
    v
HomeScreen (Con solicitud)
    |
    v
[Tap Card] --> DetailScreen
    |
    ├── [Editar] --> FormScreen (Edicion)
    ├── [Cambiar Estado] --> BottomSheet
    └── [Eliminar] --> AlertDialog --> HomeScreen
```

## Estructura de Entregables

```
MyApplication/
├── app/src/main/java/com/example/myapplication/
│   ├── viewmodel/SolicitudViewModel.kt    # Con logging y try-catch
│   └── data/repository/SolicitudRepository.kt  # Con logging
├── docs/
│   ├── INFORME_SEMANA4_DEBUGGING_OPTIMIZACION.md  # Informe tecnico
│   └── screenshots/
│       ├── logcat_crud.png                # Evidencia Logcat
│       ├── profiler_cpu.png               # Evidencia CPU Profiler
│       └── profiler_memory.png            # Evidencia Memory Profiler
└── README.md
```

## Verificacion de Funcionalidad

1. Compilar y ejecutar la app
2. Verificar estado vacio inicial
3. Crear nueva solicitud con FAB
4. **Verificar indicador de carga durante guardado**
5. **Verificar que boton se deshabilita durante guardado**
6. Seleccionar tipo de servicio (AlertDialog)
7. Completar formulario y guardar
8. Ver solicitud en lista (HomeScreen)
9. Abrir detalle de solicitud
10. Cambiar estado (ModalBottomSheet)
11. Editar solicitud
12. Eliminar solicitud (AlertDialog confirmacion)
13. Cerrar y reabrir app - verificar persistencia
14. Probar con TalkBack para validar accesibilidad

## Autor

Desarrollado para Duoc UC - Curso Desarrollo App Moviles II

## Licencia

Este proyecto es para fines educativos.
