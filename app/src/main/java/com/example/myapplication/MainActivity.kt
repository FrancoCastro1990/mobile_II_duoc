package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.NavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val TAG_MEMORY = "SolicitudMemory"
        private const val MEMORY_CHECK_INTERVAL = 10_000L // 10 segundos

        /**
         * Registra el uso actual de memoria de la aplicacion.
         * Funcion helper sin referencia estatica a Activity (buena practica).
         */
        fun logMemoryUsage(context: String) {
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            val maxMemory = runtime.maxMemory() / 1024 / 1024
            val freeMemory = runtime.freeMemory() / 1024 / 1024
            val totalMemory = runtime.totalMemory() / 1024 / 1024
            val percentage = (usedMemory * 100) / maxMemory

            Log.i(TAG_MEMORY, "═══ Memoria [$context] ═══")
            Log.i(TAG_MEMORY, "  Usada: ${usedMemory}MB / ${maxMemory}MB ($percentage%)")
            Log.i(TAG_MEMORY, "  Libre: ${freeMemory}MB | Total asignada: ${totalMemory}MB")
        }
    }

    // Handler para monitoreo periodico de memoria (con cleanup en onDestroy)
    private val memoryHandler = Handler(Looper.getMainLooper())
    private val memoryCheckRunnable = object : Runnable {
        override fun run() {
            logMemoryUsage("Monitoreo periodico")
            memoryHandler.postDelayed(this, MEMORY_CHECK_INTERVAL)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate - Activity creada")
        logMemoryUsage("onCreate")

        // Iniciar monitoreo periodico de memoria
        memoryHandler.postDelayed(memoryCheckRunnable, MEMORY_CHECK_INTERVAL)
        Log.d(TAG_MEMORY, "Monitoreo de memoria iniciado (cada ${MEMORY_CHECK_INTERVAL / 1000}s)")

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // IMPORTANTE: Remover callbacks del Handler para prevenir memory leaks
        // Sin esta linea, el Handler mantendria una referencia a la Activity
        // despues de ser destruida, causando un memory leak
        memoryHandler.removeCallbacks(memoryCheckRunnable)
        Log.d(TAG_MEMORY, "Monitoreo de memoria detenido - callbacks removidos")
        logMemoryUsage("onDestroy")
        Log.d(TAG, "onDestroy - Activity destruida, recursos liberados")
    }
}
