#include <Arduino.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <stdlib.h>

BluetoothSerial Bluetooth;

struct Packet {
	String type;
	String message;
};

Packet convertStringtoMessage(String string) {
  Packet message;

  int index = string.indexOf("|");

	message.type = string.substring(0,index);
	message.message = string.substring(index + 1);


  return message;
}

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");

	// Why is this not working?
  while (!Serial)
    ;

}

void loop() {
	if(Bluetooth.available()) {
		String message = Bluetooth.readStringUntil('\n');
	}
}
