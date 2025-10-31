package com.example.sosphone

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.AlarmClock
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sosphone.databinding.ActivityConfBinding
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Calendar

/**
 * Activity de Configuración Inicial.
 *
 * Esta pantalla es responsable de capturar y guardar los datos esenciales del usuario
 * (teléfono, email, URL, ubicación) en SharedPreferences.
 *
 * Si detecta que el teléfono (campo obligatorio) ya está configurado,
 * redirige automáticamente a [MainActivity].
 *
 * También gestiona la "vuelta" desde [MainActivity] (a través de [onNewIntent] y [onResume])
 * para modificar campos específicos.
 */
class ConfActivity : AppCompatActivity() {

    private lateinit var confBinding: ActivityConfBinding
    private lateinit var sharedFich: SharedPreferences
    private lateinit var nameSharedPhone: String
    private lateinit var nameSharedEmail: String
    private lateinit var nameSharedUbication: String
    private lateinit var nameSharedUrl: String

    /**
     * Método principal del ciclo de vida. Se llama al crear la Activity.
     * Infla la vista (ViewBinding), inicializa las SharedPreferences [initPreferentShared]
     * y ejecuta la lógica principal [start].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confBinding = ActivityConfBinding.inflate(layoutInflater)
        setContentView(confBinding.root)

        initPreferentShared()
        start()
    }

    /**
     * Inicializa la instancia de SharedPreferences y carga los nombres (claves)
     * de los campos desde `strings.xml` para usarlos en toda la clase.
     */
    private fun initPreferentShared() {
        val nameSharedFich = getString(R.string.name_preferen_shared_fich)
        this.nameSharedPhone = getString(R.string.name_shared_phone)
        this.nameSharedEmail = getString(R.string.name_shared_email)
        this.nameSharedUrl = getString(R.string.name_sared_url)
        this.nameSharedUbication = getString(R.string.name_shared_ubication)

        this.sharedFich = getSharedPreferences(nameSharedFich, Context.MODE_PRIVATE)
    }

    /**
     * Se llama cada vez que la Activity vuelve a primer plano.
     *
     * Comprueba si el Intent (actualizado por [onNewIntent]) contiene extras especiales
     * ("back_phone", "back_email", etc.). Esto indica que el usuario vuelve
     * desde [MainActivity] para editar un campo.
     *
     * Si es así, limpia el campo correspondiente y muestra un Toast
     * para que el usuario introduzca un nuevo valor.
     */
    override fun onResume() {
        super.onResume()

        val retPhone = intent.getBooleanExtra("back_phone", false)
        if (retPhone) {
            confBinding.editPhone.setText("")
            Toast.makeText(this, R.string.msg_new_phone, Toast.LENGTH_LONG).show()
            intent.removeExtra("back_phone")
        }

        val retEmail = intent.getBooleanExtra("back_email", false)
        if (retEmail) {
            confBinding.editEmail.setText("")
            Toast.makeText(this, R.string.msg_empty_email, Toast.LENGTH_LONG).show()
            intent.removeExtra("back_email")
        }

        val retUrl = intent.getBooleanExtra("back_url", false)
        if (retUrl) {
            confBinding.editUrl.setText("")
            Toast.makeText(this, "Introduce la nueva URL", Toast.LENGTH_LONG).show()
            intent.removeExtra("back_url")
        }

        val retUbication = intent.getBooleanExtra("back_ubication", false)
        if (retUbication) {
            confBinding.editGps.setText("")
            Toast.makeText(this, "Introduce la nueva Ubicación", Toast.LENGTH_LONG).show()
            intent.removeExtra("back_ubication")
        }
    }

    /**
     * Lógica principal de la Activity.
     *
     * 1. Carga todos los datos guardados de SharedPreferences.
     * 2. Si el teléfono (campo obligatorio) ya existe, navega a [MainActivity] y cierra esta.
     * 3. Si no existe, rellena los campos de texto con los datos guardados (si los hay).
     * 4. Configura el listener del botón "Guardar" (`btnConf`).
     * 5. Al pulsar "Guardar", valida los campos (teléfono obligatorio, email y URL opcionales),
     * guarda todo en SharedPreferences y navega a [MainActivity].
     */
    private fun start() {

        val sharedPhone: String? = sharedFich.getString(nameSharedPhone, null)
        val sharedEmail: String? = sharedFich.getString(nameSharedEmail, null)
        val sharedUrl: String? = sharedFich.getString(nameSharedUrl, null)
        val sharedUbication: String? = sharedFich.getString(nameSharedUbication, null)


        if (sharedPhone != null) {
            startMainActivity(sharedPhone, sharedEmail, sharedUrl, sharedUbication)
            finish()
            return
        }

        confBinding.editPhone.setText(sharedPhone)
        confBinding.editEmail.setText(sharedEmail)
        confBinding.editUrl.setText(sharedUrl)
        confBinding.editGps.setText(sharedUbication)

        confBinding.btnConf.setOnClickListener {
            val numberPhone = confBinding.editPhone.text.toString()
            val email = confBinding.editEmail.text.toString()
            val url = confBinding.editUrl.text.toString()
            val ubication = confBinding.editGps.text.toString()


            if (numberPhone.isEmpty()) {
                Toast.makeText(this, R.string.msg_empty_phone, Toast.LENGTH_LONG).show()
            }else if (!isValidPhoneNumber2(numberPhone, "ES")) {
                Toast.makeText(this, R.string.msg_not_valid_phone, Toast.LENGTH_LONG).show()
            }else if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "El formato del email no es válido", Toast.LENGTH_LONG).show()
            }else if (url.isNotEmpty() && !Patterns.WEB_URL.matcher(url).matches()) {
                Toast.makeText(this, "El formato de la URL no es válido", Toast.LENGTH_LONG).show()
            }else{
                val edit = sharedFich.edit()
                edit.putString(nameSharedPhone, numberPhone)
                edit.putString(nameSharedEmail, email.ifEmpty { null })
                edit.putString(nameSharedUrl, url.ifEmpty { null })
                edit.putString(nameSharedUbication, ubication.ifEmpty { null })
                edit.apply()

                startMainActivity(
                    numberPhone,
                    email.ifEmpty { null },
                    url.ifEmpty { null },
                    ubication.ifEmpty { null }
                )
            }
        }

        confBinding.btnAlarma.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 2)

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)


            val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
                putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
                putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "Alarma SOS")
            }
            startActivity(intent)
        }
    }


    /**
     * Inicia [MainActivity] y le pasa todos los datos de configuración como extras.
     * Utiliza flags (CLEAR_TOP, SINGLE_TOP) para asegurar que [MainActivity]
     * no se apile múltiples veces y reciba los datos en [onNewIntent].
     *
     * @param phone El número de teléfono (Obligatorio).
     * @param email El email del usuario (Opcional, puede ser null).
     * @param url La URL asociada (Opcional, puede ser null).
     * @param ubication La ubicación guardada (Opcional, puede ser null).
     */
    private fun startMainActivity(phone: String, email: String?, url: String?, ubication: String?) {
        val intent = Intent(this@ConfActivity, MainActivity::class.java)
        intent.apply {
            putExtra(getString(R.string.string_phone), phone)
            putExtra(getString(R.string.string_email), email)
            putExtra(getString(R.string.string_ubication), ubication)
            putExtra(getString(R.string.string_url), url)

            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    /**
     * Validador de teléfono simple usando las utilidades base de Android.
     * (No se usa actualmente, se prefiere [isValidPhoneNumber2]).
     *
     * @param phoneNumber El número a validar.
     * @return `true` si es un número global válido.
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)
    }

    /**
     * Validador de teléfono avanzado usando la librería 'libphonenumber' de Google.
     * Es más preciso y permite validación regional.
     *
     * @param phoneNumber El número de teléfono a validar (ej. "600111222").
     * @param countryCode El código del país (ej. "ES" para España) para la validación regional.
     * @return `true` si el número es válido para la región, `false` si ocurre un error o no es válido.
     */
    fun isValidPhoneNumber2(phoneNumber: String, countryCode: String): Boolean {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val number = phoneUtil.parse(phoneNumber, countryCode)
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Se llama cuando esta Activity (ConfActivity) recibe un nuevo Intent mientras
     * ya está en la pila (ej. cuando [MainActivity] vuelve a nosotros usando
     * los flags SINGLE_TOP o CLEAR_TOP).
     *
     * Es crucial actualizar el Intent de la Activity con [setIntent] para que
     * [onResume] pueda leer los nuevos datos (extras) que vienen en el Intent.
     *
     * @param intent El nuevo Intent que recibe la Activity.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}