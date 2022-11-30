#include <Arduino.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <stdlib.h>

BluetoothSerial Bluetooth;

#define SER Serial

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");
	WiFi.mode(WIFI_STA);
	WiFi.disconnect();

  // Why is this not working?
  while (!Serial);
}

void loop() {
	if(SER.available()) {
		String packet = SER.readStringUntil('\n');
	}	
}
