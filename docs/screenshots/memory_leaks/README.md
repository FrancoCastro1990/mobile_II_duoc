# Capturas de Memory Leaks - Semana 5

## Capturas a tomar en Android Studio

### Antes de correccion (documentado en informe)
1. `leak_handler_antes.png` - Codigo con Handler sin cleanup en onDestroy
2. `leak_static_antes.png` - Codigo con referencia estatica a Activity (companion object con instance = this)
3. `profiler_memoria_antes.png` - Memory Profiler mostrando crecimiento de heap

### Despues de correccion
4. `leak_handler_despues.png` - Codigo corregido con handler.removeCallbacks() en onDestroy
5. `leak_static_despues.png` - Codigo corregido sin referencia estatica a Activity
6. `profiler_memoria_despues.png` - Memory Profiler mostrando memoria estable
7. `leakcanary_no_leaks.png` - LeakCanary mostrando 0 leaks detectados
8. `logcat_memoria.png` - Logcat con filtro `tag:SolicitudMemory` mostrando logs de memoria

### Como tomar las capturas

1. **Memory Profiler**: View > Tool Windows > Profiler > Seleccionar proceso > Memory
2. **LeakCanary**: Ejecutar app en debug, navegar entre pantallas, revisar notificacion de LeakCanary
3. **Logcat**: Filtrar con `tag:SolicitudMemory` en la barra de filtros de Logcat
