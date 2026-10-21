/*
 * ============================================================================
 * TI3042 — Aplicaciones Móviles para IoT (Evaluación Sumativa 2 y Unidad 3)
 * Proyecto: Auto_IoT_Control (ESP32 SoftAP + HC-SR04 + L298N + LED Alerta)
 * Red SoftAP: SSID "Auto_IoT_Control" | IP: 192.168.4.1 | Puerto HTTP: 80
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

  // Endpoint raíz (/) compatible con panel web y scraping de respaldo
  server.on("/", HTTP_GET, []() {
    verificarFrenoEmergencia();
    bool alerta = (distanciaActual <= DISTANCIA_SEGURIDAD);
    String html = "<html><head><meta charset='utf-8'></head><body>";
    html += "<h1>Auto_IoT_Control</h1>";
    html += "<p>Distancia: " + String(distanciaActual) + " cm</p>";
    html += "<p>Estado Motor: " + estadoMotor + "</p>";
    html += "<p>Seguridad: " + String(alerta ? "⚠️ FRENO ACTIVO" : "VÍA DESPEJADA") + "</p>";
    html += "</body></html>";
    server.send(200, "text/html", html);
  });

  server.begin();
}

void loop() {
  verificarFrenoEmergencia();
  server.handleClient();
  delay(40);
}
