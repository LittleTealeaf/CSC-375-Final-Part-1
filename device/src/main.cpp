#include <Arduino.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <stdlib.h>

BluetoothSerial Bluetooth;

struct Packet {
	String type;
	String content;
};

Packet convertStringtoPacket(String string) {
  Packet message;

  int index = string.indexOf("|");

	message.type = string.substring(0,index);
	message.content = string.substring(index + 1);


  return message;
}

void handleConnectPacket(Packet packet) {

}

void handleQueryPacket(Packet packet) {

}

void handlePacket(Packet packet) {
	if(packet.type.equals("connect")) {
		return handleConnectPacket(packet);
	}
	if(packet.type.equals("query")) {
		return handleQueryPacket(packet);
	}
}

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");

	// Why is this not working?
  while (!Serial)
    ;

}

void loop() {
	if(Serial.available()) {
		String message = Serial.readStringUntil('\n');

		Packet packet = convertStringtoPacket(message);

		handlePacket(packet);
	}
}
