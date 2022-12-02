#include <Arduino.h>
#include <BluetoothSerial.h>
#include <M5Core2.h>
#include <Ticker.h>
#include <WiFi.h>
#include <stdlib.h>
#include <string.h>

#define SEP ":"
#define EOL '\n'

char VERSION[] = "1";
char NO_CONTENT[] = "";
char NO_SSID[] = "NO_SSID";
char NO_PASSWORD[] = "NO_PASSWORD";

String WifiSSID = "";
String WifiPassword = "";
int wifiStatus;

BluetoothSerial Bluetooth;

void sendPacket(const char *topic, char *content) {
  if (Bluetooth.connected()) {
    Bluetooth.printf("%s%s%s\n", topic, SEP, content);
  }
}

void sendWiFiStatus() {
  int status = WiFi.status();
  char *content = (char *)std::to_string(status).c_str();
  sendPacket("WIFI/STATUS", content);
}

void recievePacket(String packet) {
  int sep_index = packet.indexOf(SEP);
  String topic = packet.substring(0, sep_index);
  String content = packet.substring(sep_index + 1);

  if (Serial) {
    Serial.println(packet.begin());
  }

  if (topic.equals("DEVICE/GET_VERSION")) {
    sendPacket("DEVICE/VERSION", VERSION);
  } else if (topic.equals("WIFI/GET_STATUS")) {
    sendWiFiStatus();
  } else if (topic.equals("WIFI/SET_SSID")) {
    WifiSSID = content;
  } else if (topic.equals("WIFI/SET_PASSWORD")) {
    WifiPassword = content;
  } else if (topic.equals("WIFI/GET_SSID")) {
    sendPacket("WIFI/SSID", WifiSSID.begin());
  } else if (topic.equals("WIFI/GET_PASSWORD")) {
    sendPacket("WIFI/PASSWORD", WifiPassword.begin());
  } else if (topic.equals("WIFI/DO_CONNECT")) {
    WiFi.begin(WifiSSID.begin(), WifiPassword.begin());
  } else if (topic.equals("WIFI/GET_LOCAL_IP")) {
    sendPacket("WIFI/LOCAL_IP", WiFi.localIP().toString().begin());
  } else {
    sendPacket("PACKET/UNKNOWN", topic.begin());
    return;
  }

  sendPacket("PACKET/SUCCESS", topic.begin());
}

void updateWiFi() {
  int current = WiFi.status();
  if (current != wifiStatus) {
    wifiStatus = current;
    sendWiFiStatus();
  }
}

void setup() {
  M5.begin();
  Serial.begin(115200);
  Bluetooth.begin("Littletealeaf/CSC-375-Final");
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
}

void loop() {
  if (Bluetooth.available()) {
    String packet = Bluetooth.readStringUntil(EOL);
    recievePacket(packet);
  }
  updateWiFi();
}
