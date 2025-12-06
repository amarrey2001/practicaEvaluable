package com.example.sosphone

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.AlarmClock
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sosphone.databinding.ActivityConfBinding
import java.util.Calendar

/**
 * Activity de Configuración Nativa con Spinner incluido.
 */
class ConfActivity : AppCompatActivity() {

    private lateinit var confBinding: ActivityConfBinding
    private lateinit var sharedFich: SharedPreferences

    // Claves SharedPreferences
    private lateinit var nameSharedPhone: String
    private lateinit var nameSharedEmail: String
    private lateinit var nameSharedUbication: String
    private lateinit var nameSharedUrl: String
    private lateinit var nameSharedDate: String

    private val KEY_CHECK_TERMS = "DATA_TERMS"
    private val KEY_RADIO_SEX = "DATA_SEX"
    private val KEY_SPINNER_POS = "DATA_SPINNER_POS" // Clave nueva para el spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confBinding = ActivityConfBinding.inflate(layoutInflater)
        setContentView(confBinding.root)

        initPreferentShared()
        start()
    }

    private fun initPreferentShared() {
        val nameSharedFich = getString(R.string.name_preferen_shared_fich)
        this.nameSharedPhone = getString(R.string.name_shared_phone)
        this.nameSharedEmail = getString(R.string.name_shared_email)
        this.nameSharedUrl = getString(R.string.name_sared_url)
        this.nameSharedUbication = getString(R.string.name_shared_ubication)
        this.nameSharedDate = "DATA_DATE"

        this.sharedFich = getSharedPreferences(nameSharedFich, Context.MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("back_phone", false)) {
            confBinding.editPhone.setText("")
            confBinding.editPhone.setText("")
            confBinding.editEmail.setText("")
            confBinding.editUrl.setText("")
            confBinding.editGps.setText("")
            confBinding.editDate.setText("")
            intent.removeExtra("back_phone")
        }
    }

    private fun start() {
        // 1. Configurar Spinner
        // Definimos las opciones
        val opciones = arrayOf("Familiar", "Amigo", "Trabajo", "Servicios Emergencia", "Otro")

        // Creamos el adaptador nativo de Android
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        confBinding.spinnerParentesco.adapter = adapter

        // 2. Cargar datos guardados
        val sharedPhone = sharedFich.getString(nameSharedPhone, null)
        val sharedEmail = sharedFich.getString(nameSharedEmail, null)
        val sharedUrl = sharedFich.getString(nameSharedUrl, null)
        val sharedUbication = sharedFich.getString(nameSharedUbication, null)
        val sharedDate = sharedFich.getString(nameSharedDate, null)
        val sharedTerms = sharedFich.getBoolean(KEY_CHECK_TERMS, false)
        val sharedSexId = sharedFich.getInt(KEY_RADIO_SEX, -1)

        // Cargar posición del spinner (por defecto 0 -> primera opción)
        val sharedSpinnerPos = sharedFich.getInt(KEY_SPINNER_POS, 0)

        // Si ya existe teléfono (configuración hecha), ir a Main
        if (sharedPhone != null) {
            startMainActivity(sharedPhone, sharedEmail, sharedUrl, sharedUbication, sharedDate)
            finish()
            return
        }

        // 3. Rellenar UI
        confBinding.editPhone.setText(sharedPhone)
        confBinding.editEmail.setText(sharedEmail)
        confBinding.editUrl.setText(sharedUrl)
        confBinding.editGps.setText(sharedUbication)
        confBinding.editDate.setText(sharedDate)
        confBinding.cbTerminos.isChecked = sharedTerms

        if (sharedSexId != -1) confBinding.radioGroupSexo.check(sharedSexId)

        // Restaurar selección del Spinner
        if (sharedSpinnerPos >= 0 && sharedSpinnerPos < opciones.size) {
            confBinding.spinnerParentesco.setSelection(sharedSpinnerPos)
        }

        // 4. Listeners
        confBinding.editDate.setOnClickListener { mostrarDatePicker() }

        confBinding.btnConf.setOnClickListener {
            val numberPhone = confBinding.editPhone.text.toString().trim()
            val email = confBinding.editEmail.text.toString().trim()
            val url = confBinding.editUrl.text.toString().trim()
            val ubication = confBinding.editGps.text.toString().trim()
            val date = confBinding.editDate.text.toString().trim()

            val isTermsAccepted = confBinding.cbTerminos.isChecked
            val selectedRadioId = confBinding.radioGroupSexo.checkedRadioButtonId

            // Obtener posición seleccionada del spinner
            val selectedSpinnerPos = confBinding.spinnerParentesco.selectedItemPosition

            if (numberPhone.isEmpty()) {
                Toast.makeText(this, R.string.msg_empty_phone, Toast.LENGTH_LONG).show()
            } else if (!isValidPhoneNumberNative(numberPhone)) {
                Toast.makeText(this, R.string.msg_not_valid_phone, Toast.LENGTH_LONG).show()
            } else if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_LONG).show()
            } else if (!isTermsAccepted) {
                Toast.makeText(this, "Debes aceptar los términos", Toast.LENGTH_LONG).show()
            } else {
                // Guardar
                val edit = sharedFich.edit()
                edit.putString(nameSharedPhone, numberPhone)
                edit.putString(nameSharedEmail, email.ifEmpty { null })
                edit.putString(nameSharedUrl, url.ifEmpty { null })
                edit.putString(nameSharedUbication, ubication.ifEmpty { null })
                edit.putString(nameSharedDate, date.ifEmpty { null })
                edit.putBoolean(KEY_CHECK_TERMS, isTermsAccepted)
                edit.putInt(KEY_RADIO_SEX, selectedRadioId)

                // Guardar posición del spinner
                edit.putInt(KEY_SPINNER_POS, selectedSpinnerPos)

                edit.apply()

                startMainActivity(numberPhone, email, url, ubication, date)
            }
        }

        confBinding.btnAlarma.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 2)
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
                putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Alarma SOS")
            }
            startActivity(intent)
        }
    }

    private fun isValidPhoneNumberNative(phoneNumber: String): Boolean {
        return Patterns.PHONE.matcher(phoneNumber).matches()
    }

    private fun mostrarDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            confBinding.editDate.setText("$d/${m + 1}/$y")
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun startMainActivity(phone: String, email: String?, url: String?, ubication: String?, date: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(getString(R.string.string_phone), phone)
            putExtra(getString(R.string.string_email), email)
            putExtra(getString(R.string.string_ubication), ubication)
            putExtra(getString(R.string.string_url), url)
            putExtra("extra_date", date)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}