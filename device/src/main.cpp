#include <Arduino.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <stdlib.h>

BluetoothSerial Bluetooth;

struct Packet {
  String type;
  String content;
  bool error;
};

Packet convertStringtoPacket(String string) {
  Packet packet;
	packet.error = false;

  int index = string.indexOf("|");

  if (index == -1) {
    packet.error = true;
    return packet;
  }

  packet.type = string.substring(0, index);
  packet.content = string.substring(index + 1);

  return packet;
}

void handleConnectPacket(Packet packet) {
  Serial.println("Handling Connect Packet");
  Serial.println(packet.content);
}

void handleQueryPacket(Packet packet) {
  Serial.println("Handling Query Packet");
  Serial.println(packet.content);
}

void handlePacket(Packet packet) {
  if (packet.type.equals("connect")) {
    return handleConnectPacket(packet);
  }
  if (packet.type.equals("query")) {
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
  if (Serial.available()) {
    String message = Serial.readStringUntil('\n');

    Packet packet = convertStringtoPacket(message);

    if (!packet.error) {
      handlePacket(packet);
    }
  }
}
