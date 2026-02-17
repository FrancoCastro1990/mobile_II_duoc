# Capturas de Pantalla

## Semana 4 - Debugging y Optimizacion

### Capturas Incluidas

| Archivo | Descripcion | Criterio |
|---------|-------------|----------|
| `logcat_crud.png` | Logcat con filtro `tag:SolicitudCRUD` mostrando flujo de crear solicitud | Criterio 2 |
| `profiler_cpu.png` | CPU Profiler - Find CPU Hotspots (Java/Kotlin Method Recording) | Criterio 4 |
| `profiler_memory.png` | Memory Profiler - Analyze Memory Usage (Heap Dump) | Criterio 4 |

### Filtros de Logcat

```
tag:SolicitudCRUD           # Operaciones CRUD
tag:SolicitudPerformance    # Metricas de rendimiento
tag:SolicitudValidation     # Validacion de formulario
tag:SolicitudDB             # Operaciones de base de datos
```

---

## Semana 5 - Memory Leaks

### Capturas Incluidas

Las capturas se encuentran en `memory_leaks/`:

| Archivo | Descripcion | Criterio |
|---------|-------------|----------|
| `memory_leaks/profiler_memoria_antes.png` | Memory Profiler mostrando heap antes de correcciones | Diagnostico |
| `memory_leaks/profiler_memoria_despues.png` | Memory Profiler mostrando heap estable despues de correcciones | Validacion |
| `memory_leaks/leakcanary_no_leaks.png` | LeakCanary mostrando 0 leaks detectados | Validacion |
| `memory_leaks/logcat_memoria.png` | Logcat con filtro `tag:SolicitudMemory` | Evidencia |

### Filtros de Logcat para Memoria

```
tag:SolicitudMemory         # Metricas de uso de memoria
tag:SolicitudMemory level:info   # Solo metricas (sin debug)
```

### Informe Completo

Ver `docs/INFORME_SEMANA5_MEMORY_LEAKS.md` para el informe tecnico con analisis antes/despues.
