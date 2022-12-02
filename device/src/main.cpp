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

char *WifiSSID;
char *WifiPassword;
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

  sendPacket("PACKET/RECEIVED", topic.begin());

  if (topic.equals("DEVICE/GET_VERSION")) {
    sendPacket("DEVICE/VERSION", VERSION);
  } else if (topic.equals("WIFI/GET_STATUS")) {
    sendWiFiStatus();
  } else if (topic.equals("WIFI/SET_SSID")) {
    WifiSSID = content.begin();
  } else if (topic.equals("WIFI/SET_PASSWORD")) {
    WifiPassword = content.begin();
  } else if (topic.equals("WIFI/GET_SSID")) {
    if (WifiSSID == nullptr) {
      sendPacket("WIFI/SSID", NO_CONTENT);
    } else {
      sendPacket("WIFI/SSID", WifiSSID);
    }
  } else if (topic.equals("WIFI/GET_PASSWORD")) {
    if (WifiPassword == nullptr) {
      sendPacket("WIFI/PASSWORD", NO_CONTENT);
    } else {
      sendPacket("WIFI/PASSWORD", WifiPassword);
    }
  } else if (topic.equals("WIFI/DO_CONNECT")) {
    if (WifiSSID == nullptr) {
      sendPacket("WIFI/ERROR", NO_SSID);
    } else if (WifiPassword == nullptr) {
      sendPacket("WIFI/ERROR", NO_PASSWORD);
    } else {
      WiFi.begin(WifiSSID, WifiPassword);
    }
  } else if(topic.equals("WIFI/GET_LOCAL_IP")) {
		sendPacket("WIFI/LOCAL_IP", WiFi.localIP().toString().begin());
	}
}

void updateWiFi() {
	int current = WiFi.status();
	if(current != wifiStatus) {
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
