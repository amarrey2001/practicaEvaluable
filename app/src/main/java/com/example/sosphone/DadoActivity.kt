package com.example.sosphone

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech // Importar TTS
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sosphone.databinding.ActivityDadoBinding
import java.util.Locale // Importar Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDadoBinding
    private lateinit var handler: Handler
    private var suma: Int = 0

    // 1. Variable para el TextToSpeech
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Configuramos la voz al iniciar
        configureTextToSpeech()

        initEvents()
    }

    // Configuración del motor de voz
    private fun configureTextToSpeech() {
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                // Configuramos idioma español
                textToSpeech.language = Locale("es", "ES")
            } else {
                Log.e("TTS", "Error al inicializar TextToSpeech")
            }
        }
    }

    private fun initEvents() {
        binding.imageButton.setOnClickListener {
            binding.imageButton.isEnabled = false
            binding.txtResultado.visibility = View.INVISIBLE

            // Si estaba hablando de una tirada anterior, que se calle
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }

            game()
        }
    }

    private fun game() {
        scheduleRun()
    }

    private fun scheduleRun() {
        handler = Handler(Looper.getMainLooper())
        val schedulerExecutor = Executors.newSingleThreadScheduledExecutor()

        for (i in 0..10) {
            schedulerExecutor.schedule({
                handler.post {
                    throwDadoInTime()
                }
            }, (i * 200).toLong(), TimeUnit.MILLISECONDS)
        }

        schedulerExecutor.schedule({
            handler.post {
                mostrarResultadoFinal()
                binding.imageButton.isEnabled = true
            }
        }, 2500, TimeUnit.MILLISECONDS)

        schedulerExecutor.shutdown()
    }

    private fun throwDadoInTime() {
        val numDados = Array(3) { Random.nextInt(1, 7) }
        val imageViews = arrayOf(
            binding.imagviewDado1,
            binding.imagviewDado2,
            binding.imagviewDado3
        )

        suma = numDados.sum()

        for (i in imageViews.indices) {
            selectView(imageViews[i], numDados[i])
        }
    }

    private fun selectView(imgV: ImageView, v: Int) {
        when (v) {
            1 -> imgV.setImageResource(R.drawable.dado1)
            2 -> imgV.setImageResource(R.drawable.dado2)
            3 -> imgV.setImageResource(R.drawable.dado3)
            4 -> imgV.setImageResource(R.drawable.dado4)
            5 -> imgV.setImageResource(R.drawable.dado5)
            6 -> imgV.setImageResource(R.drawable.dado6)
        }
    }

    private fun mostrarResultadoFinal() {
        binding.txtResultado.text = suma.toString()
        binding.txtResultado.visibility = View.VISIBLE

        val chiste = ChistesAlmacen.obtenerChiste(suma)
        speakResult(chiste)
    }

    private fun speakResult(texto: String) {
        if (::textToSpeech.isInitialized) {
            textToSpeech.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}