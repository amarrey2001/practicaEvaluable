package com.example.sosphone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sosphone.databinding.ActivityPpalBinding
import java.net.URLEncoder

/**
 * Activity Principal (Pantalla de Acciones).
 *
 * Esta es la pantalla principal de la aplicación. Muestra los datos de contacto
 * configurados (teléfono, email, etc.) y proporciona botones de acción rápida para:
 * 1. Realizar una llamada de emergencia.
 * 2. Abrir una URL en el navegador.
 * 3. Abrir una ubicación en la app de mapas.
 * 4. Enviar un email.
 *
 * También gestiona el permiso de llamada (CALL_PHONE) y permite al usuario
 * volver a la [ConfActivity] para reconfigurar los datos.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityPpalBinding

    private var phoneSOS: String? = null
    private var emailSOS: String? = null
    private var urlSOS: String? = null
    private var ubicationSOS: String? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var permisionPhone = false


    /**
     * Método principal del ciclo de vida. Se llama al crear la Activity.
     * Aquí se infla la vista (ViewBinding) y se inicializan los componentes
     * y listeners principales.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityPpalBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        init()
        initActionButtons()
    }

    /**
     * Se llama cada vez que la Activity vuelve a primer plano.
     * Es el lugar ideal para refrescar la UI con los datos más recientes
     * (ya que podemos volver desde ConfActivity con datos nuevos).
     */
    override fun onResume() {
        super.onResume()
        permisionPhone = isPermissionCall()

        phoneSOS = intent.getStringExtra(getString(R.string.string_phone))
        emailSOS = intent.getStringExtra(getString(R.string.string_email))
        urlSOS = intent.getStringExtra(getString(R.string.string_url))
        ubicationSOS = intent.getStringExtra(getString(R.string.string_ubication))

    }

    /**
     * Inicializa los componentes que solo necesitan configurarse una vez,
     * como el launcher de permisos y el botón para ir a la configuración.
     */
    private fun init() {
        registerLauncher()

        if (!isPermissionCall())
            requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)

        mainBinding.ivChangePhone.setOnClickListener {
            val nameSharedFich = getString(R.string.name_preferen_shared_fich)

            val nameSharedPhone = getString(R.string.name_shared_phone)
            val nameSharedEmail = getString(R.string.name_shared_email)
            val nameSharedUrl = getString(R.string.name_sared_url)
            val nameSharedUbication = getString(R.string.name_shared_ubication)

            val sharedFich = getSharedPreferences(nameSharedFich, Context.MODE_PRIVATE)
            val edit = sharedFich.edit()
            edit.remove(nameSharedPhone)
            edit.remove(nameSharedEmail)
            edit.remove(nameSharedUrl)
            edit.remove(nameSharedUbication)
            edit.apply()

            val intent = Intent(this, ConfActivity::class.java )
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

                    putExtra("back_phone", true)
                    putExtra("back_email",true)
                    putExtra("back_url",true)
                    putExtra("back_ubication",true)
                }
            startActivity(intent)
        }
    }

    /**
     * Registra el "Activity Result Launcher" para manejar la respuesta
     * del usuario a la solicitud de permiso de CALL_PHONE.
     */
    private fun registerLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                permisionPhone = true
            } else {
                Toast.makeText(
                    this, "Necesitas habilitar los permisos de llamada",
                    Toast.LENGTH_LONG
                ).show()
                goToConfiguracionApp()
            }
        }
    }

    /**
     * Configura los listeners OnClick para los 4 botones de acción principales.
     * (Llamar, Abrir URL, Abrir Mapas, Enviar Email).
     *
     * ADVERTENCIA: Los Intents implícitos (URL, Mapas, Email) no usan try-catch.
     * Si el usuario no tiene una app que pueda manejar la acción (ej. no tiene
     * app de mapas), la aplicación CRASHEARÁ.
     * La forma segura es usar `intent.resolveActivity(packageManager) != null`.
     */
    private fun initActionButtons() {

        mainBinding.button.setOnClickListener {
            permisionPhone = isPermissionCall()
            if (permisionPhone) {
                call()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
            }
        }

        mainBinding.btnOpenUrl.setOnClickListener {
            if (urlSOS.isNullOrEmpty()) {
                Toast.makeText(this, "No hay URL configurada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var completeUrl = urlSOS!!
            if (!completeUrl.startsWith("http://") && !completeUrl.startsWith("https://")) {
                completeUrl = "http://$completeUrl"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(completeUrl))
            startActivity(intent)
        }

        mainBinding.btnOpenLocation.setOnClickListener {
            if (ubicationSOS.isNullOrEmpty()) {
                Toast.makeText(this, "No hay ubicación configurada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val encodedAddress = Uri.encode(ubicationSOS)
            val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedAddress")
            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            startActivity(intent)
        }

        mainBinding.btnOpenEmail.setOnClickListener {
            if (emailSOS.isNullOrEmpty()) {
                Toast.makeText(this, "No hay email configurado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailSOS")
                putExtra(Intent.EXTRA_SUBJECT, "Asunto del correo")
                putExtra(Intent.EXTRA_TEXT, "Cuerpo del mensaje")
            }

            startActivity(intent)
        }
    }


    /**
     * Comprueba si la app tiene permiso para realizar llamadas (CALL_PHONE).
     * Delega la comprobación real a [isPermissionToUser] si la versión
     * de Android es M (API 23) o superior.
     *
     * @return `true` si el permiso está concedido o no es necesario (APIs < 23),
     * `false` si el permiso está denegado y se necesita (APIs >= 23).
     */
    private fun isPermissionCall(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        } else {
            return isPermissionToUser()
        }
    }

    /**
     * Verifica si el permiso CALL_PHONE ha sido concedido explícitamente por el usuario.
     *
     * @return `true` si el permiso [PackageManager.PERMISSION_GRANTED], `false` en caso contrario.
     */
    private fun isPermissionToUser(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Inicia una llamada telefónica directa (ACTION_CALL) usando el [phoneSOS].
     * Requiere que el permiso [isPermissionCall] sea `true`.
     * Si [phoneSOS] es nulo o vacío, muestra un Toast.
     */
    private fun call() {
        if (phoneSOS.isNullOrEmpty()) {
            Toast.makeText(this, "No hay teléfono de emergencia configurado", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneSOS")
        }
        startActivity(intent)
    }

    /**
     * Abre la pantalla de configuración de permisos específica para esta aplicación.
     * Útil si el usuario deniega un permiso y queremos facilitarle que lo active.
     */
    private fun goToConfiguracionApp() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /**
     * Se llama cuando esta Activity (MainActivity) recibe un nuevo Intent mientras
     * ya está en la pila (ej. cuando volvemos desde ConfActivity con los flags
     * SINGLE_TOP o CLEAR_TOP).
     *
     * Es crucial actualizar el Intent de la Activity con [setIntent] para que
     * [onResume] pueda leer los nuevos datos (extras).
     *
     * @param intent El nuevo Intent que recibe la Activity.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}