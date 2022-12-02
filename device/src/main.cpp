#include <Arduino.h>
#include <BluetoothSerial.h>
#include <Ticker.h>
#include <WiFi.h>
#include <stdlib.h>
#include <string.h>
#include <M5Core2.h>

#define SEP ":"
#define EOL '\n'

const char *NO_CONTENT = "";

BluetoothSerial Bluetooth;

void sendPacket(const char* topic, char *content) {
	if(Bluetooth.connected()) {
		Bluetooth.printf("%s%s%s\n", topic, SEP, content);
	}
}

void recievePacket(String packet) {
	int sep_index = packet.indexOf(SEP);
	String topic = packet.substring(0, sep_index);
	String content = packet.substring(sep_index + 1);
	
	if(topic.equals("DEVICE/COMPATIBLE")) {

	}
}

void updateWiFi() {
}

void setup() {
	M5.begin();
	Serial.begin(115200);
	Bluetooth.begin("Littletealeaf/CSC-375-Final");
	WiFi.mode(WIFI_STA);
	WiFi.disconnect();
}

void loop() {
	if(Bluetooth.available()) {
		String packet = Bluetooth.readStringUntil(EOL);
		recievePacket(packet);
	}
	updateWiFi();
}

