package com.example.sosphone

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val tvFecha = findViewById<TextView>(R.id.tv_fecha_actual)

        // 1. Accedemos al mismo archivo de preferencias que usa ConfActivity
        // Usamos getString(R.string.name_preferen_shared_fich) para que coincida con tu configuración
        val nameSharedFich = getString(R.string.name_preferen_shared_fich)
        val sharedFich = getSharedPreferences(nameSharedFich, Context.MODE_PRIVATE)

        // 2. Recuperamos la fecha guardada
        // En ConfActivity la clave era "DATA_DATE"
        val fechaGuardada = sharedFich.getString("DATA_DATE", null)

        // 3. Mostramos la fecha
        if (!fechaGuardada.isNullOrEmpty()) {
            // Si el usuario guardó una fecha en el DatePicker
            tvFecha.text = "Fecha configurada:\n$fechaGuardada"
        } else {
            // Si no hay fecha guardada, mostramos la actual como respaldo
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())
            tvFecha.text = "No has configurado ninguna fecha.\n(Hoy es: $currentDate)"
        }
    }
}