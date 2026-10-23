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
  - Se enciende el indicador visual rojo **`⚠️ FRENO ACTIVO`** y el **`LED ALERTA: ON`** en Rojo Carmín (`#EF4444`).
  - El botón **`ADELANTE`** (Verde suave `#10B981`) se deshabilita visual y funcionalmente para impedir colisiones frontales, permitiendo únicamente `DETENER` (Rojo carmín `#EF4444`) o `ATRÁS` (Ámbar `#F59E0B`).
- **Si `distancia > 15 cm`**:
  - El indicador muestra **`VÍA DESPEJADA`** en Verde oliva/esmeralda suave (`#10B981`) y se habilita la tracción hacia adelante.

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

### Estación 1: Ciberseguridad ISO/IEC 27400 (Login Seguro, SHA-256 y EncryptedSharedPreferences)
1. Abrir la aplicación: la pantalla presenta un diseño de inicio de sesión estándar y profesional, con los campos limpios y vacíos (sin credenciales prellenadas en pantalla para proteger la privacidad del operador).
2. **Credenciales preconfiguradas para la evaluación docente:**
   - **Usuario:** `admin`
   - **Contraseña:** `admin123` (o clave corta de laboratorio `1234`)
3. Tocar **"ENTRAR AL PANEL"**: la aplicación procesa internamente la clave calculando su digestión unidireccional **SHA-256** (`MessageDigest.getInstance("SHA-256")`) y comparándola contra el hash cifrado almacenado en `EncryptedSharedPreferences` (`AES-256-SIV` para claves y `AES-256-GCM` para valores respaldado por `AndroidKeyStore`), asegurando **cero contraseñas en texto plano** conforme a la norma ISO/IEC 27400.
4. También permite registrar nuevos operadores tocando el enlace inferior *"¿Registrar nuevo operador? Toca aquí"*.
5. Cerrar completamente la app y volver a abrirla: comprobar que la **sesión persiste de forma cifrada** y entra directamente al Dashboard. Presionar el botón de **Cerrar Sesión** (esquina superior derecha) para verificar la destrucción segura de la sesión.

### Estación 2: Permisos en Runtime y Manejo de Denegación
1. Al iniciar por primera vez (o desde Ajustes quitando los permisos de Ubicación/Dispositivos cercanos), rechazar los permisos solicitados.
2. Verificar que la aplicación **NO se cierra ni crashea**, mostrando el **Banner y Diálogo ISO/IEC 27400** que explica por qué se requieren los permisos y ofreciendo los botones **"Volver a Solicitar"** y **"Configuración"**.

### Estación 3: Interconexión Inalámbrica Local P2P y Modo Prueba en Banco
1. **Conexión entre Dos Dispositivos:**
   - Teléfono 1 (Auto / Rol A): Presionar *"ACTIVAR AUTO"*. La app abre el `ServerSocket` TCP en el puerto 8888 y muestra el número IP local en pantalla grande (ej. `192.168.1.45`).
   - Teléfono 2 (Control / Rol B): En la pestaña *"📱 A otro Celular"*, escribir el número IP del Teléfono 1 y presionar *"CONECTAR AL AUTO"*.
2. **Mandos de Conducción:**
   - Presionar **`ADELANTE`**: el vehículo avanza y el sensor de distancia en ambos teléfonos muestra cómo el auto se aproxima al obstáculo (-3 cm cada 200 ms).
   - **Demostración de la Regla Crítica (<= 15 cm):** Al alcanzar los 15 cm, el vehículo activa automáticamente el freno (`DETENIDO`), enciende **`⚠️ FRENO ACTIVO`**, prende el **`LED ALERTA: ON`** y bloquea el botón **`ADELANTE`**.
   - Presionar **`ATRÁS`**: el auto retrocede (+3 cm cada 200 ms) alejándose del peligro hasta despejar la vía (**`VÍA DESPEJADA`**).
3. **Modo Prueba en Banco (Sin Vehículo Físico):**
   - En el Rol A (El Auto), se dispone del panel *"MODO PRUEBA (SIMULACIÓN EN BANCO)"* con mandos locales directos (`AVANZAR`, `DETENER`, `ATRÁS`), permitiendo al docente evaluar la cinemática y el corte de emergencia a 15 cm directamente en un solo dispositivo cuando no se disponga del chasis físico.

### Estación 4: Tolerancia a Fallos y Rotación de Pantalla
1. Con la conexión activa, rotar el teléfono a horizontal (Landscape) y vertical (Portrait): comprobar que la conexión socket TCP **no se corta** y la interfaz se adapta fluidamente.
2. Desconectar abruptamente el Wi-Fi o cerrar el servidor: verificar que la estación pasa a estado `"RECONECTANDO"` con reintento automático sin crashear.

### Estación 3: Interconexión en Vivo entre Dos Dispositivos (Par-a-Par TCP 8888) y Freno a 15 cm
1. Conectar el **Teléfono 1** y el **Teléfono 2** a la misma red Wi-Fi local (o zona Wi-Fi portátil de uno de los teléfonos).
2. En el **Teléfono 1**, elegir **`ROL A: NODO SENSOR`** y presionar **"INICIAR SERVIDOR TCP NODO SENSOR (PUERTO 8888)"**. Anotar la IP local mostrada en pantalla (ej. `192.168.43.10`).
3. En el **Teléfono 2**, elegir **`ROL B: ESTACIÓN`**, verificar que el Switch esté en **"Modo Par-a-Par Android (Socket TCP :8888)"**, ingresar la IP del Teléfono 1 y presionar **"CONECTAR"**.
4. Mover el **Slider de Distancia** en el Teléfono 1: observar cómo la distancia se actualiza en tiempo real (cada `200 ms`) en el Teléfono 2.
5. Presionar **`ADELANTE`** en el Teléfono 2 con distancia `> 15 cm`: el estado pasa a **`AVANZANDO`** en ambos teléfonos.
6. Bajar el Slider en el Teléfono 1 a **`<= 15 cm`** (o tocar el chip **`8 cm (Freno!)`**): comprobar que se activa **`⚠️ FRENO ACTIVO`**, el motor pasa automáticamente a **`DETENIDO`** y el botón **`ADELANTE`** queda bloqueado en la Estación de Control.

### Estación 4: Rotación de Pantalla y Tolerancia a Fallos (Desconexión Forzada)
1. Con la conexión activa entre ambos dispositivos, **rotar el teléfono de Portrait a Landscape**: verificar que la interfaz se reorganiza en dos paneles sin perder la sesión ni desconectar el socket TCP.
2. En el **Teléfono 1 (Nodo Sensor)**, presionar **"DETENER AUTO"** o apagar el Wi-Fi abruptamente: verificar que el **Teléfono 2 (Estación de Control)** captura la excepción en `Dispatchers.IO`, cambia el indicador superior a **`RECONECTANDO`** sin congelar la interfaz, y al volver a encender el servidor en el Teléfono 1 se **reconecta automáticamente**.

---

## 7. Identidad Visual y Paleta Cromática (Tablero de Diagnóstico OEM Automotriz)

La interfaz gráfica fue rediseñada íntegramente con una paleta automotriz profesional estilo tablero de diagnóstico OEM (inspirada en la interfaz de BMW iDrive y escáneres OBD2):

- **Fondo general (Canvas):** Gris antracita / pizarra muy oscuro mate (`#191C21`). Sin negros puros ni azules brillantes.
- **Contenedores y Tarjetas (Cards):** Gris grafito un tono más claro (`#262B33`), esquinas redondeadas y separadores/bordes sutiles en gris oscuro (`#313742`).
- **Tipografía:**
  - **Títulos principales y estados clave:** Blanco puro (`#FFFFFF`) o blanco frío (`#F3F4F6`).
  - **Subtítulos, etiquetas y textos secundarios:** Gris medio / ceniza (`#8C95A6`).
- **Sistema de Color Funcional (Semáforo de Diagnóstico):**
  - **Crítico / Peligro / Detener:** Rojo escarlata sobrio (`#E02E2E` o `#EF4444`).
  - **Precaución / Atención / Alertas intermedias:** Ámbar automotriz (`#F59E0B` o `#D97706`).
  - **Vía libre / Conectado / Estado óptimo:** Verde esmeralda suave (`#10B981`).
- **Botones y Controles:**
  - **Botón principal ('Entrar', 'Conectar', 'Activar Auto'):** Superficie gris grafito destacada (`#2F3642`) con borde técnico (`#3E4656`) y texto blanco en negrita (`#FFFFFF`), alternando a rojo escarlata (`#E02E2E`) cuando la acción es detener/cortar.
  - **Mandos de dirección ('Avanzar', 'Atrás'):** Fondos oscuros neutros (`#262B33`) con texto claro (`#F3F4F6`) e íconos legibles, reservando el color llamativo exclusivamente para **`DETENER`** (rojo escarlata sobrio `#E02E2E`).
- **Sensor y Consola:**
  - **Distancia del sensor:** Número grande en texto blanco limpio (`#FFFFFF`), conmutando a rojo escarlata (`#E02E2E`) únicamente cuando cae en el umbral crítico (`<= 15 cm`).
  - **Consola de telemetría:** Fondo técnico carbón (`#14161B`) con tipografía monoespaciada en gris suave (`#9CA3AF`).
- **Eliminación Total:** Cero residuos de colores cian, turquesa, rosa neón, bordes brillantes o efectos cyberpunk.


