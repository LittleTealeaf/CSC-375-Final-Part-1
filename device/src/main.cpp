#include <Arduino.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <stdlib.h>

BluetoothSerial Bluetooth;

#define CONNECTION Serial

typedef enum PacketType { QUERY_WIFI_STATUS, CONNECT_WIFI, WIFI_STATUS, ERROR } PacketType; 

/*
 * Packet Structure
 *
 * <Type><Content>
 *
 */

struct Packet {
  int type;
  String content;
};

void sendPacket(PacketType type, String content) {
	CONNECTION.printf("%d%s", type, content.begin());
}

// Handles a CONNECT_WIFI packet
void handleConnect(Packet packet) {
	int index = packet.content.indexOf('\t');
	String uuid = packet.content.substring(0,index);
	String password = packet.content.substring(index);
}

// Handles a QUERY_WIFI_STATUS packet
void handleQuery(Packet packet) {

}

void handlePacket(Packet packet) {
  if (packet.type == QUERY_WIFI_STATUS) {
    return handleQuery(packet);
  }
  if (packet.type == CONNECT_WIFI) {
    return handleConnect(packet);
  }
}

Packet createPacket(String message) {
  Packet packet;
  packet.type = message.charAt(0);
  packet.content = message.substring(1);

  return packet;
}

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");

  // Why is this not working?
  while (!Serial)
    ;
}

void loop() {
  if (CONNECTION.available()) {
    String message = CONNECTION.readStringUntil('\n');

    Packet packet = createPacket(message);
  }
}
