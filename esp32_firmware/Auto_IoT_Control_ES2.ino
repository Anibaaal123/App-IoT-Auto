/*
 * ============================================================================
 * TI3042 — Aplicaciones Móviles para IoT (Evaluación Sumativa 2 y Unidad 3)
 * Proyecto: Auto_IoT_Control (ESP32 SoftAP + HC-SR04 + L298N + LED Alerta)
 * Red SoftAP: SSID "Auto_IoT_Control" | IP: 192.168.4.1 | Puerto HTTP: 80
 * Dashboard Web: Cyber-Telemetry Cockpit con actualización dinámica en vivo
 * ============================================================================
 */

#include <WiFi.h>
#include <WebServer.h>

// Configuración de Red SoftAP
const char* AP_SSID = "Auto_IoT_Control";
const char* AP_PASS = "IoT2026Segura"; // Mínimo 8 caracteres para WPA2

// Pines Sensor Ultrasónico HC-SR04
const int PIN_TRIG = 5;
const int PIN_ECHO = 18;

// Pines Driver de Motor L298N
const int PIN_IN1 = 26;
const int PIN_IN2 = 27;
const int PIN_ENA = 14;

// Pin LED de Alerta de Proximidad
const int PIN_LED_ALERTA = 2;

// Regla crítica de negocio: Freno de emergencia a <= 15 cm
const int DISTANCIA_SEGURIDAD = 15;

WebServer server(80);
String estadoMotor = "DETENIDO";
long distanciaActual = 50;

long medirDistanciaCM() {
  digitalWrite(PIN_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(PIN_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);

  long duracion = pulseIn(PIN_ECHO, HIGH, 30000); // Timeout 30ms (~500 cm)
  if (duracion == 0) return 400;
  long cm = duracion * 0.034 / 2;
  if (cm < 2) cm = 2;
  if (cm > 400) cm = 400;
  return cm;
}

void detenerMotor() {
  digitalWrite(PIN_IN1, LOW);
  digitalWrite(PIN_IN2, LOW);
  estadoMotor = "DETENIDO";
}

void avanzarMotor() {
  digitalWrite(PIN_IN1, HIGH);
  digitalWrite(PIN_IN2, LOW);
  estadoMotor = "AVANZANDO";
}

void retrocederMotor() {
  digitalWrite(PIN_IN1, LOW);
  digitalWrite(PIN_IN2, HIGH);
  estadoMotor = "RETROCEDIENDO";
}

void verificarFrenoEmergencia() {
  distanciaActual = medirDistanciaCM();
  bool frenoActivo = (distanciaActual <= DISTANCIA_SEGURIDAD);
  digitalWrite(PIN_LED_ALERTA, frenoActivo ? HIGH : LOW);

  if (frenoActivo && estadoMotor == "AVANZANDO") {
    detenerMotor();
  }
}

void enviarJsonStatus() {
  verificarFrenoEmergencia();
  bool alerta = (distanciaActual <= DISTANCIA_SEGURIDAD);

  String json = "{";
  json += "\"distancia\":" + String(distanciaActual) + ",";
  json += "\"estado\":\"" + estadoMotor + "\",";
  json += "\"alerta\":" + String(alerta ? "true" : "false") + ",";
  json += "\"frenoActivo\":" + String(alerta ? "true" : "false");
  json += "}";

  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.send(200, "application/json", json);
}

// Dashboard Web Telemétrico Único (Aeroespacial / Cyber-HUD)
const char HTML_DASHBOARD[] PROGMEM = R"rawliteral(
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
  <title>AUTO_IOT_CONTROL // Cockpit Telemétrico</title>
  <style>
    :root {
      --bg: #191C21;
      --card: #262B33;
      --card-inner: #1E2229;
      --border: #313742;
      --primary: #2F3642;
      --primary-border: #3E4656;
      --optimal: #10B981;
      --amber: #F59E0B;
      --alert: #E02E2E;
      --text: #FFFFFF;
      --text-cold: #F3F4F6;
      --muted: #8C95A6;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace; }
    body { background: var(--bg); color: var(--text-cold); padding: 16px; min-height: 100vh; display: flex; justify-content: center; align-items: flex-start; }
    .cockpit { width: 100%; max-width: 520px; display: flex; flex-direction: column; gap: 14px; }
    
    .hud-header {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 14px 18px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 4px 16px rgba(0,0,0,0.4);
    }
    .hud-title h1 { font-size: 1.05rem; font-weight: 800; letter-spacing: 1px; color: var(--text); }
    .hud-title span { font-size: 0.72rem; color: var(--muted); text-transform: uppercase; }
    .badge-live { background: rgba(16,185,129,0.15); border: 1px solid var(--optimal); color: var(--optimal); padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 700; display: flex; align-items: center; gap: 6px; }
    .dot { width: 8px; height: 8px; border-radius: 50%; background: var(--optimal); box-shadow: 0 0 6px var(--optimal); animation: pulse 1.4s infinite; }
    @keyframes pulse { 0%,100%{opacity:1;} 50%{opacity:0.3;} }

    .telemetry-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 18px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      transition: border-color 0.3s;
    }
    .telemetry-card.danger { border-color: var(--alert); box-shadow: 0 0 20px rgba(224,46,46,0.25); }

    .gauge-box {
      background: var(--card-inner);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 18px;
      display: flex;
      justify-content: space-around;
      align-items: center;
      text-align: center;
    }
    .dist-num { font-size: 2.8rem; font-weight: 900; color: var(--text); line-height: 1; transition: color 0.3s; }
    .dist-num.danger { color: var(--alert); }
    .dist-label { font-size: 0.75rem; color: var(--muted); letter-spacing: 0.5px; margin-top: 4px; }

    .safety-banner {
      padding: 8px 14px;
      border-radius: 8px;
      font-weight: 800;
      font-size: 0.85rem;
      text-align: center;
      letter-spacing: 1px;
      background: rgba(16,185,129,0.15);
      border: 1px solid var(--optimal);
      color: var(--optimal);
      transition: all 0.3s;
    }
    .safety-banner.danger {
      background: rgba(224,46,46,0.2);
      border-color: var(--alert);
      color: #fff;
    }

    .pinout-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 8px;
      background: var(--card-inner);
      border: 1px solid var(--border);
      padding: 12px;
      border-radius: 10px;
    }
    .pin-col { text-align: center; }
    .pin-label { font-size: 0.65rem; color: var(--muted); }
    .pin-val { font-size: 0.78rem; font-weight: 700; color: var(--optimal); margin-top: 2px; }

    .controls-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 18px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .btn {
      width: 100%;
      height: 52px;
      border: 1px solid var(--border);
      border-radius: 12px;
      font-weight: 800;
      font-size: 1rem;
      letter-spacing: 1px;
      cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 8px;
      transition: transform 0.1s, opacity 0.2s;
    }
    .btn:active { transform: scale(0.98); }
    .btn-forward { background: #262B33; border: 1px solid #313742; color: #F3F4F6; transition: all 0.2s; }
    .btn-forward.active { background: rgba(16,185,129,0.22); border: 2px solid var(--optimal); color: var(--optimal); font-weight: 900; }
    .btn-forward.dimmed { opacity: 0.45; }
    .btn-forward:disabled { background: #1E2229; border-color: #262B33; color: #6B7280; cursor: not-allowed; }
    .btn-stop { background: var(--alert); border-color: var(--alert); color: #fff; }
    .btn-reverse { background: #262B33; border: 1px solid #313742; color: #fff; transition: all 0.2s; }
    .btn-reverse.active { background: rgba(245,158,11,0.22); border: 2px solid var(--amber); color: var(--amber); font-weight: 900; }
    .btn-reverse.dimmed { opacity: 0.45; }
    .btn-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
  </style>
</head>
<body>
  <div class="cockpit">
    <div class="hud-header">
      <div class="hud-title">
        <h1>AUTO_IOT_CONTROL</h1>
        <span>Cockpit Telemétrico // ESP32 SoftAP</span>
      </div>
      <div class="badge-live"><div class="dot"></div> EN VIVO</div>
    </div>

    <div id="telemetryCard" class="telemetry-card">
      <div id="safetyBanner" class="safety-banner">VÍA DESPEJADA</div>
      
      <div class="gauge-box">
        <div>
          <div id="distVal" class="dist-num">--</div>
          <div class="dist-label">RADAR HC-SR04 (CM)</div>
        </div>
        <div style="border-left: 1px solid rgba(255,255,255,0.1); height: 50px;"></div>
        <div>
          <div id="motorVal" style="font-size:1.3rem; font-weight:800; color:var(--text)">DETENIDO</div>
          <div class="dist-label">ESTADO MOTOR L298N</div>
        </div>
      </div>

      <div class="pinout-grid">
        <div class="pin-col"><div class="pin-label">ENA (G14)</div><div id="pinEna" class="pin-val">LOW</div></div>
        <div class="pin-col"><div class="pin-label">IN1 (G26)</div><div id="pinIn1" class="pin-val">LOW</div></div>
        <div class="pin-col"><div class="pin-label">IN2 (G27)</div><div id="pinIn2" class="pin-val">LOW</div></div>
        <div class="pin-col"><div class="pin-label">LED (G2)</div><div id="pinLed" class="pin-val">OFF</div></div>
      </div>
    </div>

    <div class="controls-card">
      <button id="btnForward" class="btn btn-forward" onclick="sendCmd('/adelante')">▲ ADELANTE</button>
      <div class="btn-row">
        <button class="btn btn-stop" onclick="sendCmd('/parar')">■ PARAR</button>
        <button id="btnReverse" class="btn btn-reverse" onclick="sendCmd('/atras')">▼ ATRÁS</button>
      </div>
    </div>
  </div>

  <script>
    async function updateTelemetry() {
      try {
        const res = await fetch('/status');
        const data = await res.json();
        const dist = data.distancia;
        const motor = data.estado;
        const isBrake = dist <= 15 || data.alerta;

        document.getElementById('distVal').innerText = dist + ' cm';
        document.getElementById('motorVal').innerText = motor;

        const card = document.getElementById('telemetryCard');
        const banner = document.getElementById('safetyBanner');
        const distNum = document.getElementById('distVal');
        const btnFwd = document.getElementById('btnForward');
        const btnRev = document.getElementById('btnReverse');

        if (isBrake) {
          card.classList.add('danger');
          banner.classList.add('danger');
          banner.innerText = '⚠️ FRENO ACTIVO (OBSTÁCULO <= 15 CM)';
          distNum.classList.add('danger');
          btnFwd.disabled = true;
          btnFwd.innerText = '🚫 ADELANTE BLOQUEADO';
          document.getElementById('pinLed').innerText = 'ON';
        } else {
          card.classList.remove('danger');
          banner.classList.remove('danger');
          banner.innerText = 'VÍA DESPEJADA (DISTANCIA SEGURA)';
          distNum.classList.remove('danger');
          btnFwd.disabled = false;
          btnFwd.innerText = '▲ ADELANTE';
          document.getElementById('pinLed').innerText = 'OFF';
        }

        // Iluminación activa del botón en marcha y atenuación del inactivo
        if (motor === 'AVANZANDO') {
          btnFwd.classList.add('active');
          btnFwd.classList.remove('dimmed');
          btnRev.classList.remove('active');
          btnRev.classList.add('dimmed');
          if (!isBrake) btnFwd.innerText = '▲ ADELANTE (EN MARCHA ●)';
          btnRev.innerText = '▼ ATRÁS';
        } else if (motor === 'RETROCEDIENDO') {
          btnRev.classList.add('active');
          btnRev.classList.remove('dimmed');
          btnFwd.classList.remove('active');
          btnFwd.classList.add('dimmed');
          btnRev.innerText = '▼ ATRÁS (EN MARCHA ●)';
          if (!isBrake) btnFwd.innerText = '▲ ADELANTE';
        } else {
          btnFwd.classList.remove('active', 'dimmed');
          btnRev.classList.remove('active', 'dimmed');
          if (!isBrake) btnFwd.innerText = '▲ ADELANTE';
          btnRev.innerText = '▼ ATRÁS';
        }

        if (motor === 'AVANZANDO') {
          document.getElementById('pinEna').innerText = 'HIGH';
          document.getElementById('pinIn1').innerText = 'HIGH';
          document.getElementById('pinIn2').innerText = 'LOW';
        } else if (motor === 'RETROCEDIENDO') {
          document.getElementById('pinEna').innerText = 'HIGH';
          document.getElementById('pinIn1').innerText = 'LOW';
          document.getElementById('pinIn2').innerText = 'HIGH';
        } else {
          document.getElementById('pinEna').innerText = 'LOW';
          document.getElementById('pinIn1').innerText = 'LOW';
          document.getElementById('pinIn2').innerText = 'LOW';
        }
      } catch(e) {}
    }

    async function sendCmd(url) {
      try {
        await fetch(url);
        updateTelemetry();
      } catch(e) {}
    }

    setInterval(updateTelemetry, 450);
    updateTelemetry();
  </script>
</body>
</html>
)rawliteral";

void setup() {
  Serial.begin(115200);

  pinMode(PIN_TRIG, OUTPUT);
  pinMode(PIN_ECHO, INPUT);
  pinMode(PIN_IN1, OUTPUT);
  pinMode(PIN_IN2, OUTPUT);
  pinMode(PIN_ENA, OUTPUT);
  pinMode(PIN_LED_ALERTA, OUTPUT);

  digitalWrite(PIN_ENA, HIGH);
  detenerMotor();

  // Inicializar SoftAP en 192.168.4.1
  WiFi.mode(WIFI_AP);
  WiFi.softAP(AP_SSID, AP_PASS);
  Serial.print("SoftAP iniciado. IP: ");
  Serial.println(WiFi.softAPIP());

  // Nuevo Endpoint JSON nativo (/status) para App Android ES2
  server.on("/status", HTTP_GET, []() {
    enviarJsonStatus();
  });

  // Endpoints de control compatibles con Evaluación 1 y Evaluación 2
  server.on("/adelante", HTTP_GET, []() {
    verificarFrenoEmergencia();
    if (distanciaActual > DISTANCIA_SEGURIDAD) {
      avanzarMotor();
    } else {
      detenerMotor();
    }
    enviarJsonStatus();
  });

  server.on("/atras", HTTP_GET, []() {
    verificarFrenoEmergencia();
    retrocederMotor();
    enviarJsonStatus();
  });

  server.on("/parar", HTTP_GET, []() {
    detenerMotor();
    enviarJsonStatus();
  });

  // Endpoint raíz (/) con el Dashboard Telemétrico Cyber-Cockpit
  server.on("/", HTTP_GET, []() {
    server.send_P(200, "text/html", HTML_DASHBOARD);
  });

  server.begin();
}

void loop() {
  verificarFrenoEmergencia();
  server.handleClient();
  delay(40);
}
