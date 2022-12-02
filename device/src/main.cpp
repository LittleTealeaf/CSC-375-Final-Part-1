#include <Arduino.h>
#include <BluetoothSerial.h>
#include <Ticker.h>
#include <WiFi.h>
#include <stdlib.h>
#include <string.h>

BluetoothSerial Bluetooth;

#define DELIMINER "|"
#define PACKET_TERMINATOR '\n'

int wifiStatus;
Ticker wifiStatusTicker;

// Sends a packet, as long as the serial connection is valid
void sendPacket(const char topic[], char *content) {
  if (Bluetooth.connected()) {
    Bluetooth.printf("%s%s%s\n", topic, DELIMINER, content);
    if (Serial) {
      Serial.printf("Send Message: %s%s%s", topic, DELIMINER, content);
      Serial.println();
    }
  }
}

void sendWiFiStatusPacket() {
  int status = WiFi.status();
  char *content = (char *)std::to_string(status).c_str();
  sendPacket("WIFI/STATUS", content);
}

// Checks wifi status, and if it changes, sends an updated status packet
void checkWiFiStatus() {
  int status = WiFi.status();
  if (status != wifiStatus) {
    wifiStatus = status;
    sendWiFiStatusPacket();
  }
}

void handlePacket(String packet) {
  if (Serial) {
    Serial.printf("Received Packet: %s", packet.begin());
    Serial.println();
  }
  // Finds the packet topic
  int index_topic = packet.indexOf(DELIMINER);
  String topic = index_topic == -1 ? packet : packet.substring(0, index_topic);
  String content = index_topic == -1 ? "" : packet.substring(index_topic + 1);

  if (topic.equals("WIFI/QUERY")) {
    return sendWiFiStatusPacket();
  }

  if (topic.equals("WIFI/CONNECT")) {
    int index_credentials = content.indexOf(',');
    String ssid = content.substring(0, index_credentials);
    String password = content.substring(index_credentials + 1);
    WiFi.begin(ssid.begin(), password.begin());
  }
}

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();

  wifiStatusTicker.attach_ms(5000, checkWiFiStatus);
}

void loop() {
  if (Bluetooth.available()) {
    Serial.println("Recieving Data");
    String packet = Bluetooth.readStringUntil(PACKET_TERMINATOR);
    handlePacket(packet);
  }
}
