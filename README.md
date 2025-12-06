# SOS Phone: Aplicación de Configuración y Acciones de Emergencia

Este proyecto en Kotlin para Android implementa una aplicación de dos pantallas. Su objetivo es proporcionar una interfaz simple para configurar datos de contacto de emergencia y ofrecer acciones rápidas para usarlos.

1.  **`ConfActivity`**: Pantalla de configuración inicial de datos de emergencia (teléfono, email, URL y ubicación).
2.  **`MainActivity`**: Pantalla principal con botones de acción rápida (Llamar, Enviar Email, Abrir URL, Abrir Ubicación, etc.).

---

## 1. Configuración (`ConfActivity`)

Esta Activity es el punto de entrada de la aplicación y se enfoca en la recopilación y validación de los datos esenciales.

### Persistencia de Datos
* **`SharedPreferences`**: Los datos se almacenan de forma persistente utilizando la API de `SharedPreferences`.
* **Campos Opcionales**: Los campos opcionales (Email, URL, Ubicación) se guardan como `null` si están vacíos (`.ifEmpty { null }`).

### Flujo de Navegación
* **Acceso Rápido**: Si al iniciar, detecta que el teléfono (campo obligatorio) ya está configurado, redirige automáticamente a `MainActivity`.
* **Vuelta para Modificar**: Gestiona la vuelta desde `MainActivity` utilizando `onNewIntent()` y `onResume()`. Al recibir *extras* específicos (`"back_phone"`, `"back_email"`, etc.), limpia el campo correspondiente y muestra un `Toast` para guiar al usuario.

### Validación de Campos
El botón "Guardar" (`btnConf`) ejecuta las siguientes validaciones:
* **Teléfono (Obligatorio)**: Se valida utilizando la librería **`com.google.i18n.phonenumbers.PhoneNumberUtil`** (`isValidPhoneNumber2`) para garantizar la precisión regional (código "ES").
* **Email (Opcional)**: Si no está vacío, se valida usando la expresión regular estándar de Android (`Patterns.EMAIL_ADDRESS`).
* **URL (Opcional)**: Si no está vacío, se valida usando la expresión regular estándar de Android (`Patterns.WEB_URL`).

### Funcionalidad de Alarma
* Incluye un botón (`btnAlarma`) que utiliza un `Intent` implícito (`android.provider.AlarmClock.ACTION_SET_ALARM`) para programar una alarma del sistema dos minutos después.

---

## 2. Acciones Principales (`MainActivity`)

Esta Activity es la pantalla operativa, diseñada para ejecutar acciones de emergencia con los datos configurados.

### Carga de Datos
* Los datos de emergencia (`phoneSOS`, `emailSOS`, etc.) se reciben y se actualizan en `onResume()` leyendo los *extras* del `Intent`.

### Gestión de Permisos de Llamada
* **Permiso Dinámico**: Utiliza **`ActivityResultLauncher`** para solicitar en tiempo de ejecución el permiso **`android.Manifest.permission.CALL_PHONE`**.
* **Manejo de Denegación**: Si el usuario deniega el permiso, se proporciona un acceso directo a la configuración de la aplicación (`goToConfiguracionApp()`) para activarlo manually.

### Botones de Acción Rápida
Cada botón utiliza un `Intent` implícito para ejecutar una función del sistema:

| Botón                | Acción                                  | Descripción                                                      | Intent y URI |
|:---------------------|:----------------------------------------|:-----------------------------------------------------------------| :-- |
| `buttonCall`         | **Llamar**                              | Realiza una llamada directa al número de emergencia.             | `Intent.ACTION_CALL` con `Uri.parse("tel:$phoneSOS")` |
| `btnOpenUrl`         | **Abrir URL**                           | Abre la URL configurada en el navegador.                         | `Intent.ACTION_VIEW` con `Uri.parse(completeUrl)` |
| `btnOpenLocation`    | **Abrir Ubicación**                     | Abre la aplicación de mapas con la ubicación configurada.        | `Intent.ACTION_VIEW` con `Uri.parse("geo:0,0?q=$encodedAddress")` |
| `btnOpenEmail`       | **Enviar Email**                        | Abre la aplicación de correo con el destinatario preestablecido. | `Intent.ACTION_SENDTO` con `Uri.parse("mailto:$emailSOS")` |
| `btnAbrirJuegoDados` | **Lanzar dado**                         | Abre la aplicación de juego de dados y cuenta un chiste          | `startActivityForResult` para `DiceActivity` |
| `btnAbrirCalendario` | **Mostrar fecha**                       | Abre un activity en el que se muestra la fecha seleccionada   | `startActivityForResult` para `DateActivity` |

### Retorno a Configuración
* El botón de configuración (`iv_change_phone`) borra todos los datos guardados en `SharedPreferences` y lanza `ConfActivity`, forzando al usuario a reconfigurar la aplicación.

---

## 3. Consideraciones Técnicas Generales

* **ViewBinding**: Se utiliza `ActivityConfBinding` y `ActivityPpalBinding` para un acceso seguro a los elementos de la interfaz.
* **Ciclo de Vida de Activities**: Se utiliza `onNewIntent()` junto a los *flags* `FLAG_ACTIVITY_CLEAR_TOP` y `FLAG_ACTIVITY_SINGLE_TOP` para gestionar eficientemente la navegación entre la pantalla de configuración y la principal.
* **Funcionamiento en otras API**: En mi dispositivo utilizo una API 35, se hicieron pruebas en API 24 y API 25 la cuál no funcionaba ni entraba a la aplicación

---

Enlace del video https://drive.google.com/file/d/1Np0B24zUKkuIVRJljjW50STKFBJ2QKOz/view?usp=sharing
Enlace a GitHub https://github.com/amarrey2001/practicaEvaluable

---

Para más información sobre el ciclo de vida de las actividades de Android, puede consultar la documentación oficial de Google: [El ciclo de vida de la actividad](https://developer.android.com/guide/components/activities/activity-lifecycle).# PracticaEvaluable2
# PracticaEvaluable2
