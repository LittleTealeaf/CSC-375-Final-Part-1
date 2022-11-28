#include <WiFi.h>

const char* ssid = "takwashnak-server";
const char* password = "mypassword";



void setup() {
  // put your setup code here, to run once:
  WiFi.softAP(ssid,password);

}

void loop() {
  // put your main code here, to run repeatedly:

}
