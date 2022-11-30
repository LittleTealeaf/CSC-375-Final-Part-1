#include <Arduino.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <stdlib.h>

BluetoothSerial Bluetooth;

struct Message {
	char *command;
	char *content;
};

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");
}

void loop() {

  if (Bluetooth.available()) {
    String message = Bluetooth.readStringUntil('\n');
  }
}
