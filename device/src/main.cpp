#include <Arduino.h>
#include <BluetoothSerial.h>

BluetoothSerial Bluetooth;

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");
}

void loop() {

  if (Bluetooth.available()) {
    String message = Bluetooth.readStringUntil('\n');
  }
}
