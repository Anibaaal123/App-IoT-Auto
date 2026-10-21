# TI3042 — Aplicaciones Móviles para IoT (Evaluación Sumativa 2 y Preparación Unidad 3)

## Proyecto: **Auto_IoT_Control (Edición Ciberseguridad ISO/IEC 27400 & Telemetría P2P/ESP32)**

---

## 1. Problemática Resuelta y Justificación Arquitectónica

En la **Evaluación Sumativa 1**, el vehículo autónomo **Auto_IoT_Control** (basado en microcontrolador **ESP32** en modo SoftAP `192.168.4.1`, sensor ultrasónico **HC-SR04** y puente H **L298N**) carecía de controles de acceso criptográficos en la capa de aplicación móvil, gestión resiliente del ciclo de vida de Android y comunicación bidireccional de baja latencia entre dispositivos móviles.

Para esta **Evaluación Sumativa 2 (y preparación para la Unidad 3)** se diseñó e implementó una aplicación nativa en **Kotlin con Jetpack Compose (Material Design 3)** bajo tres pilares de ingeniería:

1. **Seguridad por Diseño (Norma ISO/IEC 27400 e ISO/IEC 27001):**
   - **Cero credenciales en texto plano:** Las credenciales del operador nunca se almacenan ni comparan en texto claro. Toda contraseña ingresada se transforma mediante una función resumen unidireccional **SHA-256** (`MessageDigest.getInstance("SHA-256")`) y se compara en tiempo constante (`MessageDigest.isEqual`).
   - **Bóveda Cifrada (`EncryptedSharedPreferences`):** Tanto el hash SHA-256 del operador como el token de sesión activa se persisten utilizando `androidx.security:security-crypto:1.1.0-alpha06` con esquema **`AES256_SIV`** para claves y **`AES256_GCM`** para valores, respaldados por el `AndroidKeyStore`.
   - **Gestión Resiliente de Permisos en Runtime:** Se solicitan dinámicamente los permisos de cercanía y red según la versión de Android (`NEARBY_WIFI_DEVICES` en Android 13+, `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT` en Android 12+, `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`). Si el operador deniega los permisos, la aplicación **no sufre cierres inesperados (crashes)** ni bloqueos; despliega un banner/diálogo explicativo alineado con ISO 27400 e inicia un modo degradado seguro por IP directa.

2. **Arquitectura Reactiva MVVM (`ViewModel` + `StateFlow`):**
   - Toda la lógica de negocio, el servidor TCP (`ServerSocket`), el cliente TCP (`Socket`) y el cliente HTTP (`HttpURLConnection`) residen en `IoTViewModel`.
   - Al rotar el dispositivo entre **Portrait y Landscape**, la actividad se adapta mediante un diseño canónico de 1 o 2 columnas (`BoxWithConstraints`) **sin perder la sesión autenticada ni cortar el socket TCP/HTTP activo**.

3. **Concurrencia Estricta en `Dispatchers.IO` y Tolerancia a Fallos:**
   - Ninguna operación de red se ejecuta en el hilo principal (`Main`).
   - Ante desconexiones abruptas (apagado de Wi-Fi, cierre forzado en el otro equipo o pérdida de energía en el ESP32), los bloques `try / catch (IOException | SocketException) / finally` cierran los descriptores de socket limpiamente (`socket.close()`) y activan el estado `"RECONECTANDO"` con reintento automático cada 2.2 segundos.

---

## 2. Regla Crítica de Negocio: Freno de Seguridad a 15 cm

El sistema aplica la constante `DISTANCIA_SEGURIDAD = 15` (cm) tanto en el **Nodo Sensor / ESP32** como en la **Estación de Control**:
- **Si `distancia <= 15 cm`**:
  - Se activa inmediatamente el freno de emergencia automático: si `estadoMotor == "AVANZANDO"`, pasa de forma forzada a `"DETENIDO"`.
  - Se enciende el indicador visual rojo **`⚠️ FRENO ACTIVO`** y el **`LED ALERTA: ON`**.
  - El botón **`ADELANTE`** (`#4CAF50`) se deshabilita visual y funcionalmente para impedir colisiones frontales, permitiendo únicamente `DETENER` (`#F44336`) o `ATRÁS` (`#2196F3`).
- **Si `distancia > 15 cm`**:
  - El indicador muestra **`VÍA DESPEJADA`** y se habilita la tracción hacia adelante.

---

## 3. Especificación del Protocolo de Comunicación Inalámbrica Local

### 3.1. Modo Par-a-Par Android (Socket TCP — Puerto `8888`)
- **Rol A (`NODO SENSOR / VEHÍCULO`)**: Abre un `ServerSocket` en el puerto `8888` y muestra la IPv4 local del teléfono (ej. `192.168.1.45:8888`).
- **Flujo de Telemetría (Servidor -> Cliente cada `200 ms`)**: Envía una trama JSON terminada en salto de línea (`\n`):
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
- **Flujo de Comandos (Cliente -> Servidor)**: Envía comandos en texto UTF-8 terminados en `\n`:
  - `ADELANTE` (Ejecuta avance solo si `distancia > 15 cm`; de lo contrario fuerza `DETENIDO`).
  - `DETENER` (Detiene inmediatamente el puente H L298N).
  - `ATRAS` (Activa retroceso del motor).

### 3.2. Modo Microcontrolador ESP32 (HTTP — IP `192.168.4.1`, Puerto `80`)
- **Comandos de Actuación (HTTP GET)**:
  - `GET http://192.168.4.1/adelante`
  - `GET http://192.168.4.1/parar`
  - `GET http://192.168.4.1/atras`
- **Lectura de Telemetría (HTTP GET cada `400 ms`)**:
  - Consulta prioritariamente `GET http://192.168.4.1/status`, recibiendo:
    ```json
    {"distancia": 50, "estado": "DETENIDO", "alerta": false, "frenoActivo": false}
    ```
  - Si el ESP32 tiene el firmware original de la Evaluación 1 sin `/status`, realiza *fallback* automático a `GET http://192.168.4.1/` extrayendo la distancia en cm y el estado del motor desde el HTML.

---

## 4. Instrucciones de Compilación y Generación del APK

1. Abrir el proyecto en **Android Studio Ladybug / Meerkat o superior**.
2. Esperar la sincronización de Gradle (`compileSdk = 36`, `minSdk = 24`, Kotlin + Jetpack Compose + `androidx.security:security-crypto:1.1.0-alpha06`).
3. Para compilar desde terminal o generar el APK instalable:
   ```bash
   gradle :app:assembleDebug
   ```
4. El archivo APK generado se ubicará en:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 5. Guía de Pruebas para las 4 Estaciones de Revisión en Sala

### Estación 1: Ciberseguridad ISO/IEC 27400 (Login, SHA-256 y EncryptedSharedPreferences)
1. Abrir la aplicación en la pantalla de **Autenticación de Operador IoT**.
2. Observar el **Inspector Criptográfico ISO/IEC 27400** en la parte inferior: muestra en vivo cómo la clave ingresada se transforma en un hash **SHA-256** de 64 caracteres hexadecimales sin almacenar texto plano.
3. Seleccionar la pestaña **"Registrar Clave"**, ingresar un usuario (ej. `operador_iot`) y una clave (ej. `IoT2026`), y presionar **"CIFRAR CON SHA-256 Y ENTRAR"** (o usar la clave por defecto `test` con el usuario `operador_iot` cuyo hash SHA-256 `9f86d081...` viene precalculado).
4. Cerrar completamente la app y volver a abrirla: comprobar que la **sesión persiste de forma cifrada** y entra directamente al Dashboard. Presionar el botón de **Cerrar Sesión** (esquina superior derecha) para verificar la destrucción segura de la sesión.

### Estación 2: Permisos en Runtime y Manejo de Denegación
1. Al iniciar por primera vez (o desde Ajustes quitando los permisos de Ubicación/Dispositivos cercanos), rechazar los permisos solicitados.
2. Verificar que la aplicación **NO se cierra ni crashea**, mostrando el **Banner y Diálogo ISO/IEC 27400** que explica por qué se requieren los permisos y ofreciendo los botones **"Volver a Solicitar"** y **"Configuración"**.

### Estación 3: Interconexión en Vivo entre Dos Dispositivos (Par-a-Par TCP 8888) y Freno a 15 cm
1. Conectar el **Teléfono 1** y el **Teléfono 2** a la misma red Wi-Fi local (o zona Wi-Fi portátil de uno de los teléfonos).
2. En el **Teléfono 1**, elegir **`ROL A: NODO SENSOR`** y presionar **"INICIAR SERVIDOR TCP NODO SENSOR (PUERTO 8888)"**. Anotar la IP local mostrada en pantalla (ej. `192.168.43.10`).
3. En el **Teléfono 2**, elegir **`ROL B: ESTACIÓN`**, verificar que el Switch esté en **"Modo Par-a-Par Android (Socket TCP :8888)"**, ingresar la IP del Teléfono 1 y presionar **"CONECTAR"**.
4. Mover el **Slider de Distancia** en el Teléfono 1: observar cómo la distancia se actualiza en tiempo real (cada `200 ms`) en el Teléfono 2.
5. Presionar **`ADELANTE`** en el Teléfono 2 con distancia `> 15 cm`: el estado pasa a **`AVANZANDO`** en ambos teléfonos.
6. Bajar el Slider en el Teléfono 1 a **`<= 15 cm`** (o tocar el chip **`8 cm (Freno!)`**): comprobar que se activa **`⚠️ FRENO ACTIVO`**, el motor pasa automáticamente a **`DETENIDO`** y el botón **`ADELANTE`** queda bloqueado en la Estación de Control.

### Estación 4: Rotación de Pantalla y Tolerancia a Fallos (Desconexión Forzada)
1. Con la conexión activa entre ambos dispositivos, **rotar el teléfono de Portrait a Landscape**: verificar que la interfaz se reorganiza en dos paneles sin perder la sesión ni desconectar el socket TCP.
2. En el **Teléfono 1 (Nodo Sensor)**, presionar **"DETENER SERVIDOR TCP"** o apagar el Wi-Fi abruptamente: verificar que el **Teléfono 2 (Estación de Control)** captura la excepción en `Dispatchers.IO`, cambia el indicador superior a **`RECONECTANDO`** (`"Desconectado - Reintentando conexión..."`) sin congelar la interfaz, y al volver a encender el servidor en el Teléfono 1 se **reconecta automáticamente**.
