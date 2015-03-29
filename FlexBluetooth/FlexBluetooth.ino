#include <SoftwareSerial.h>;
SoftwareSerial Bluetooth(0, 1); 

int sensor = A0;
int sensorvalue = 0;

void setup() {
  Bluetooth.begin(9600);
  Serial.begin(9600);
  delay(100); 
}

void loop() {
  sensorvalue = analogRead(sensor);
  byte flex = map(sensorvalue, 100, 240, 0, 100);
  if (Bluetooth.available()) {
    Bluetooth.println(flex);  
  Serial.println(flex);   
    
    //Bluetooth.println();
    

    }
  }

