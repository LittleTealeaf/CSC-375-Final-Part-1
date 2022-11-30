#include <Arduino.h>
#include <BluetoothSerial.h>
#include <WiFi.h>
#include <stdlib.h>

BluetoothSerial Bluetooth;

struct Message {
	char *type;
	char *message;
};

Message convertStringtoMessage(String string) {
	Message message;

	int lengthType = 0;
	int lengthMessage = 0;
	char *str = string.begin();

	for(int i = 0; i < string.length(); i++) {
		if(str[i] == '|') {
			lengthType = i;
			lengthMessage = string.length() - i;
		}
	}

	message.type = (char*) malloc(sizeof(char) * lengthType);
	message.message = (char*) malloc(sizeof(char) * lengthMessage);

	for(int i = 0; i < string.length(); i++) {
		if(i < lengthType) {
			message.type[i] = str[i];
		} else if(i > lengthType) {
			message.message[i] = str[i];
		}
	}

	return message;
}

void setup() {
  Serial.begin(115200);
  Bluetooth.begin("LittleTealeaf/CSC-375-Final");
}

void loop() {
  if (Bluetooth.available()) {
    String message = Bluetooth.readStringUntil('\n');

		//"MessageType|MessageContent>"

  }
}
