#include <Arduino.h>

#define require_serial

void setup() {
  Serial.begin(115200);

#ifdef require_serial
  while (!Serial);
#endif
}

void loop() {}
