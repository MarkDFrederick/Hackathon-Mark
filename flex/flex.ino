int sensor = A0;
int sensorvalue = 0;
const unsigned int R = 9;
const unsigned int G = 10;
const unsigned int B = 11;
int red;
int green;
int blue;


void setup()
{
  pinMode(R, OUTPUT);
  pinMode(G, OUTPUT);
  pinMode(B, OUTPUT);
  
  Serial.begin(9600);
}

void loop()
{
  sensorvalue = analogRead(sensor);
  byte note = map(sensorvalue, 100, 240, 0, 100);
  
  color(note - 100, note + 50, note);
  delay(0050);
  Serial.print(note, DEC);
  Serial.print("\n");
//  Serial.print(sensorvalue, DEC);
//  Serial.print("\n");
}

void color(unsigned int red, unsigned int green, unsigned int blue)
{
  analogWrite(R, red);
  analogWrite(G, green);
  analogWrite(B, blue);
}
