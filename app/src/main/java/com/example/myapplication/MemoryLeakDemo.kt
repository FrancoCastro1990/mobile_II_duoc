package com.example.myapplication

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.ref.WeakReference

/**
 * MemoryLeakDemo - Ejemplos educativos de Memory Leaks para LeakCanary
 *
 * Este archivo contiene ejemplos de memory leaks comunes en Android
 * y sus correcciones. Usar con LeakCanary para detectar y analizar.
 *
 * ═══════════════════════════════════════════════════════════════
 * INSTRUCCIONES PARA REPRODUCIR MEMORY LEAKS CON LEAKCANARY:
 * ═══════════════════════════════════════════════════════════════
 *
 * PASO 1: Verificar que LeakCanary esta activo
 *   - LeakCanary se activa automaticamente en builds debug
 *   - Verificar en Logcat con filtro: tag:LeakCanary
 *
 * PASO 2: Reproducir Leak 1 - Referencia estatica a Activity
 *   a) En MainActivity.onCreate(), agregar:
 *      MemoryLeakDemo.LeakExample1_StaticActivity.simulateLeak(this)
 *   b) Rotar el dispositivo varias veces (cambia configuracion)
 *   c) Esperar notificacion de LeakCanary
 *   d) Revisar el leak trace en la app de LeakCanary
 *   e) Para limpiar: MemoryLeakDemo.LeakExample1_StaticActivity.clearLeak()
 *
 * PASO 3: Reproducir Leak 2 - Handler sin cleanup
 *   a) En MainActivity.onCreate(), agregar:
 *      MemoryLeakDemo.LeakExample2_Handler.simulateLeak()
 *   b) Navegar hacia atras (cerrar Activity) antes de 30 segundos
 *   c) LeakCanary detectara que el Handler mantiene referencia al Activity
 *   d) Para limpiar: MemoryLeakDemo.LeakExample2_Handler.clearLeak()
 *
 * PASO 4: Reproducir Leak 3 - Listener sin unregister
 *   a) En MainActivity.onCreate(), agregar:
 *      MemoryLeakDemo.LeakExample3_Listener.simulateLeak(this)
 *   b) Navegar entre pantallas varias veces
 *   c) Cada navegacion agrega un listener sin removerlo
 *   d) Para limpiar: MemoryLeakDemo.LeakExample3_Listener.clearLeak()
 *
 * PASO 5: Analizar resultados
 *   - Abrir la app "Leaks" que instala LeakCanary
 *   - Revisar el leak trace para cada ejemplo
 *   - Identificar la cadena de referencias que previene el GC
 *   - Comparar con las correcciones documentadas abajo
 *
 * ═══════════════════════════════════════════════════════════════
 */
object MemoryLeakDemo {

    private const val TAG = "MemoryLeakDemo"

    // ═══════════════════════════════════════════════════════════
    // LEAK 1: Referencia estatica a Activity
    // PROBLEMA: Una variable estatica (companion object) mantiene
    // referencia a un Activity, impidiendo que el GC lo libere
    // cuando se destruye (ej: rotacion de pantalla).
    // ═══════════════════════════════════════════════════════════
    object LeakExample1_StaticActivity {

        // MAL: Lista estatica que acumula referencias a Activities destruidos
        private val leakedActivities = mutableListOf<Activity>()

        fun simulateLeak(activity: Activity) {
            Log.w(TAG, "LEAK 1: Acumulando referencia estatica a Activity")
            leakedActivities.add(activity)
            Log.w(TAG, "LEAK 1: Total Activities retenidos: ${leakedActivities.size}")
            // Cada rotacion agrega el Activity a la lista.
            // Los Activities destruidos NO pueden ser liberados por el GC
            // porque esta lista mantiene una referencia fuerte a ellos.
        }

        fun clearLeak() {
            Log.i(TAG, "LEAK 1: Limpiando ${leakedActivities.size} referencias estaticas")
            leakedActivities.clear()
        }

        // CORRECCION: Usar WeakReference
        // Una WeakReference permite que el GC libere el objeto
        // cuando ya no hay referencias fuertes a el.
        private var safeActivityRef: WeakReference<Activity>? = null

        fun safeStore(activity: Activity) {
            Log.i(TAG, "LEAK 1 CORREGIDO: Usando WeakReference para Activity")
            safeActivityRef = WeakReference(activity)
            // Si el Activity se destruye, el GC puede liberarlo
            // y safeActivityRef.get() retornara null
        }

        fun getSafeActivity(): Activity? {
            return safeActivityRef?.get()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LEAK 2: Handler con mensajes pendientes
    // PROBLEMA: Un Handler con postDelayed mantiene referencia
    // al Activity a traves del Looper. Si el Activity se destruye
    // antes de que el mensaje se ejecute, hay un leak.
    // ═══════════════════════════════════════════════════════════
    object LeakExample2_Handler {

        // MAL: Handler que no limpia sus callbacks
        private var leakyHandler: Handler? = null

        fun simulateLeak() {
            Log.w(TAG, "LEAK 2: Creando Handler con mensaje pendiente de 30 segundos")
            leakyHandler = Handler(Looper.getMainLooper())
            leakyHandler?.postDelayed({
                // Este callback mantiene referencia implicita
                Log.d(TAG, "LEAK 2: Callback ejecutado (si ves esto, el leak persiste)")
            }, 30_000) // 30 segundos
            // Si el Activity se cierra antes de 30s, el Handler
            // mantiene la referencia y causa un leak
        }

        fun clearLeak() {
            Log.i(TAG, "LEAK 2: Removiendo callbacks pendientes del Handler")
            leakyHandler?.removeCallbacksAndMessages(null)
            leakyHandler = null
        }

        // CORRECCION: Remover callbacks en onDestroy/onCleared
        // En el Activity.onDestroy() o ViewModel.onCleared():
        //   handler.removeCallbacksAndMessages(null)
        //
        // Ejemplo correcto:
        //   override fun onDestroy() {
        //       super.onDestroy()
        //       handler.removeCallbacksAndMessages(null)
        //   }
    }

    // ═══════════════════════════════════════════════════════════
    // LEAK 3: Listener sin unregister
    // PROBLEMA: Un listener/callback registrado en un singleton
    // o manager que nunca se remueve. Cada vez que se crea
    // un Activity, se registra un nuevo listener, acumulando
    // referencias.
    // ═══════════════════════════════════════════════════════════
    object LeakExample3_Listener {

        interface OnDataChangedListener {
            fun onDataChanged(data: String)
        }

        // MAL: Lista de listeners que nunca se limpia
        private val listeners = mutableListOf<OnDataChangedListener>()

        fun simulateLeak(activity: Activity) {
            Log.w(TAG, "LEAK 3: Registrando listener sin unregister")
            // Cada rotacion/recreacion del Activity agrega un listener nuevo
            val listener = object : OnDataChangedListener {
                override fun onDataChanged(data: String) {
                    // Referencia implicita al Activity externo
                    Log.d(TAG, "Datos cambiaron: $data en ${activity.localClassName}")
                }
            }
            listeners.add(listener)
            Log.w(TAG, "LEAK 3: Total listeners acumulados: ${listeners.size}")
        }

        fun clearLeak() {
            Log.i(TAG, "LEAK 3: Limpiando todos los listeners (${listeners.size})")
            listeners.clear()
        }

        // CORRECCION: Registrar y desregistrar listeners
        // En Activity:
        //   override fun onCreate() {
        //       myListener = object : OnDataChangedListener { ... }
        //       LeakExample3_Listener.register(myListener)
        //   }
        //   override fun onDestroy() {
        //       LeakExample3_Listener.unregister(myListener)
        //   }

        fun register(listener: OnDataChangedListener) {
            listeners.add(listener)
            Log.i(TAG, "LEAK 3 CORREGIDO: Listener registrado (total: ${listeners.size})")
        }

        fun unregister(listener: OnDataChangedListener) {
            listeners.remove(listener)
            Log.i(TAG, "LEAK 3 CORREGIDO: Listener removido (total: ${listeners.size})")
        }
    }
}
