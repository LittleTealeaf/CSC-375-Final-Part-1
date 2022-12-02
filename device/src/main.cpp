#include <Arduino.h>
#include <BluetoothSerial.h>
#include <Ticker.h>
#include <WiFi.h>
#include <stdlib.h>
#include <string.h>
#include <M5Core2.h>

BluetoothSerial Bluetooth;

#define DELIMINER "|"
#define PACKET_TERMINATOR '\n'


#define STATUS_IDLE TFT_DARKGREY
#define STATUS_NO_SSID TFT_ORANGE
#define STATUS_SCAN_COMPLETE TFT_OLIVE
#define STATUS_CONNECTED TFT_GREEN
#define STATUS_FAILED TFT_RED
#define STATUS_LOST TFT_BLUE
#define STATUS_DISCONNECTED TFT_NAVY


int wifiStatus;
Ticker wifiStatusTicker;

void setScreenColor(uint32_t color) {
	M5.Lcd.fillScreen(color);	
}

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
  char *content = (char *) std::to_string(status).c_str();
  sendPacket("WIFI/STATUS", content);
}

// Checks wifi status, and if it changes, sends an updated status packet
void checkWiFiStatus() {
  int status = WiFi.status();
  if (status != wifiStatus) {
		if(Serial) {
			Serial.printf("WiFi Connection Update: %d\n", status);
		}
    wifiStatus = status;
    sendWiFiStatusPacket();

		if(status == 1) {
			setScreenColor(STATUS_IDLE);
		} else if(status == 2) {
			setScreenColor(STATUS_NO_SSID);
		} else if(status == 3) {
			setScreenColor(STATUS_SCAN_COMPLETE);
		} else if(status == 4) {
			setScreenColor(STATUS_CONNECTED);
		} else if(status == 5) {
			setScreenColor(STATUS_LOST);
		} else if(status == 6) {
			setScreenColor(STATUS_DISCONNECTED);
		}
	
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

	M5.begin();

  Bluetooth.begin("LittleTealeaf/CSC-375-Final");
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();

  wifiStatusTicker.attach_ms(500, checkWiFiStatus);
}

void loop() {
  if (Bluetooth.available()) {
    Serial.println("Recieving Data");
    String packet = Bluetooth.readStringUntil(PACKET_TERMINATOR);
    handlePacket(packet);
  }
}
