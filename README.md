# Auto_IoT_Control — Telemetría Vehicular y Control Remoto P2P / ESP32

**Asignatura:** TI3042 — Aplicaciones Móviles para IoT  
**Evaluación:** Sumativa Unidad 2  
**Entorno de desarrollo:** Android Studio | Kotlin | Jetpack Compose (Material Design 3)

---

## 1. Problemática Resuelta y Justificación de Arquitectura

### 1.1 Problemática Abordada
En entornos operativos y de robótica móvil IoT, el control vehicular y la telemetría suelen presentar vulnerabilidades críticas:
- Exposición de credenciales de operador en texto plano en la capa de aplicación.
- Bloqueos de la interfaz de usuario (*ANR*) por ejecuciones síncronas de red.
- Caídas (*crashes*) o pérdida de estado ante la rotación de pantalla o denegación de permisos de conectividad.
- Falta de mecanismos automáticos de frenado ante la aproximación a obstáculos en tiempo real.

`Auto_IoT_Control` resuelve estos problemas mediante una solución nativa en Android que garantiza control bidireccional local, visualización telemétrica de proximidad y aplicación estricta de normas de seguridad industrial OT.

### 1.2 Justificación Técnica y de Arquitectura
La solución se estructura sobre el patrón de arquitectura **MVVM (Model-View-ViewModel)** y directrices de ingeniería móvil robusta:

* **Arquitectura Reactiva (ViewModel + StateFlow):**  
  Toda la lógica de conexión (sockets TCP y llamadas HTTP), telemetría y reglas de negocio residen en `IoTViewModel`. La interfaz en Jetpack Compose reacciona a flujos de estado (`StateFlow`), asegurando que al rotar el dispositivo (Portrait / Landscape) no se destruya la sesión, no se reinicie el hardware ni se pierda la conexión socket activa.
* **Seguridad y Cero Texto Plano (Alineación ISO/IEC 27400 e ISO/IEC 27001):**  
  - **Hashing Criptográfico:** Las contraseñas nunca se almacenan ni verifican en texto plano; se procesan mediante `MessageDigest` con SHA-256 y comparación en tiempo constante (`MessageDigest.isEqual`).
  - **Almacenamiento Cifrado:** El estado de sesión y los identificadores de usuario se persisten con `EncryptedSharedPreferences` (`androidx.security:security-crypto`), empleando esquemas de clave `AES256_SIV` y valor `AES256_GCM` respaldados por `AndroidKeyStore`.
* **Gestión de Permisos en Runtime:**  
  Solicitud dinámica de permisos según versión de Android (`NEARBY_WIFI_DEVICES`, `ACCESS_FINE_LOCATION`). Ante denegación por parte del usuario, la app no sufre cierres inesperados; degrada su comportamiento a un modo seguro con diálogos explicativos y enlace a ajustes.
* **Concurrencia y Resiliencia en Red (`Dispatchers.IO`):**  
  Toda operación de socket y red se ejecuta de forma asíncrona mediante corrutinas de Kotlin en el despachador de entrada/salida. Las desconexiones abruptas son capturadas por bloques `try/catch/finally` específicos (`SocketException`, `IOException`), liberando descriptores de socket y pasando la app a un estado transicional de reintento automático sin congelar la UI.
* **Regla Crítica de Seguridad Automotriz (Freno de Emergencia a 15 cm):**  
  El sistema evalúa continuamente la distancia reportada por el sensor de proximidad. Si `distancia <= 15 cm`, se activa el freno forzado (estado motor `DETENIDO`), se enciende el testigo visual de emergencia y se bloquea el mando de avance (`ADELANTE`), permitiendo únicamente retroceso (`ATRÁS`) o detención manual.

---

## 2. Especificación del Protocolo de Comunicación Inalámbrica

La aplicación implementa dos modos de operación inalámbrica local sobre redes Wi-Fi:

### 2.1 Modo Par-a-Par Android (Socket TCP — Puerto 8888)
Permite la interconexión directa entre dos dispositivos móviles:
- **Rol A (Nodo Sensor / Vehículo):** Inicializa un `ServerSocket` en el puerto `8888` y publica su dirección IPv4 local.
- **Rol B (Estación de Control Remoto):** Abre un `Socket` TCP cliente apuntando a la IP y puerto del Nodo Sensor.

#### Trama de Telemetría (Servidor $\to$ Cliente, cada 200 ms)
Transmisión en formato JSON delimitado por salto de línea (`\n`):
```json
{
  "distancia": 45,
  "estado": "DETENIDO",
  "alerta": false,
  "frenoActivo": false,
  "mensajeSeguridad": "VÍA DESPEJADA",
  "timestamp": 1729500000123
}
```

#### Comandos de Control (Cliente $\to$ Servidor)
Cadenas UTF-8 terminadas en `\n`:
- `ADELANTE`: Activa tracción si `distancia > 15 cm`. Bloqueado automáticamente si `distancia <= 15 cm`.
- `DETENER`: Fuerza el corte de tracción en el puente H / simulación.
- `ATRAS`: Activa retroceso para despejar obstáculos frontales.

---

### 2.2 Modo Microcontrolador ESP32 (HTTP REST — IP 192.168.4.1:80)
Comunicación con hardware físico en modo SoftAP Wi-Fi:

* **Lectura de Telemetría (GET cada 400 ms):**  
  - Consulta primaria: `GET http://192.168.4.1/status` $\to$ Recibe payload JSON con `{distancia, estado, alerta, frenoActivo}`.
  - Fallback: `GET http://192.168.4.1/` $\to$ Extracción y parseo de variables desde el cuerpo HTML del servidor web embebido.
* **Acciones de Conducción:**  
  - `GET http://192.168.4.1/adelante`
  - `GET http://192.168.4.1/parar`
  - `GET http://192.168.4.1/atras`

---

## 3. Instrucciones de Compilación y Ejecución

### 3.1 Requisitos de Entorno
- **IDE:** Android Studio (Ladybug / Meerkat o superior).
- **Android SDK:** `compileSdk = 34` (o superior), `minSdk = 24`.
- **JDK:** OpenJDK 17 o superior.
- **Dependencias base:** Kotlin Coroutines, Jetpack Compose, AndroidX Security Crypto (`1.1.0-alpha06`).

### 3.2 Compilación desde Terminal (Gradle Wrapper)
Clonar el repositorio y situarse en la raíz del proyecto:

```bash
# Compilar y empaquetar APK en modo Debug
./gradlew assembleDebug

# En plataformas Windows (CMD/PowerShell)
gradlew.bat assembleDebug
```

El archivo ejecutable generado se almacena en:  
`app/build/outputs/apk/debug/app-debug.apk`

### 3.3 Instalación y Despliegue en Dispositivo Físico
Asegurarse de tener habilitada la **Depuración por USB** en el dispositivo Android:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3.4 Guía Rápida de Verificación en Sala (Criterios A1 - A5)

1. **A2 · Login Seguro:**  
   Ingresar credenciales de operador por defecto (`admin` / `admin123`). Comprobar que no existen contraseñas prellenadas en texto plano y que el almacenamiento local se encuentra encriptado con AndroidKeyStore.
2. **A3 · Permisos en Runtime:**  
   Denegar permisos en el primer arranque. Comprobar que la app no crashea y despliega el banner de advertencia ISO 27400.
3. **A4 · Interconexión P2P (Dos Dispositivos):**  
   - Conectar ambos dispositivos a la misma red Wi-Fi o punto de acceso.
   - En **Teléfono 1**, seleccionar **Rol A (Auto)** y presionar **Activar Auto** (inicia el puerto `8888`).
   - En **Teléfono 2**, seleccionar **Rol B (Control Remoto)**, ingresar la IP del Teléfono 1 y pulsar **Conectar**.
   - Verificar intercambio continuo de datos telemétricos y comandos de dirección.
4. **A5 · Robustez y Freno de Emergencia:**  
   - Probar avance hacia un obstáculo y constatar que al registrar `<= 15 cm` el motor cambia a `DETENIDO` y se inhibe el botón `ADELANTE`.
   - Rotar la pantalla del dispositivo durante la conexión activa para validar que la interfaz se ajusta y el enlace socket permanece íntegro.