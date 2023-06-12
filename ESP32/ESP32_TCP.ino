#include "esp_camera.h"
#include "Arduino.h"
#include "WiFi.h"
#include "FS.h"                // SD Card ESP32
#include "SD_MMC.h"            // SD Card ESP32
#include "ESP32FtpServer.h"

// Pin definition for CAMERA_MODEL_AI_THINKER
#define PWDN_GPIO_NUM     32
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM      0
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27
#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       21
#define Y4_GPIO_NUM       19
#define Y3_GPIO_NUM       18
#define Y2_GPIO_NUM        5
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22
#define LED_BUILTIN       4
#define PE_BARRIER        16

// Variables for Camera
camera_fb_t * fb = NULL;

// Variables for WiFi
const char* ssid = "NeLux_Hotspot";
const char* password = "xxxxxxxxxxx";
IPAddress host;
WiFiClient client;
int port = 29391;

// Variables for timing
bool triggeredPE = false;
ulong millisCrossing = 0;
char timeA[30];

// Variables for FTP Server
FtpServer ftpSrv;


//=======================================================================
//                    Setup Section
//=======================================================================
void setup() {

// Configure serial interface
  Serial.begin(115200);

// Start WiFi connection
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi.");
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  host = WiFi.gatewayIP();
  Serial.println("\nConnected!");
  Serial.println(WiFi.localIP());

// Configure ESP32CAM pins
  camera_config_t config;
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(PE_BARRIER, INPUT_PULLUP);
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG; //YUV422,GRAYSCALE,RGB565,JPEG

// Configure image format and quality
  config.frame_size = FRAMESIZE_SVGA;
  config.jpeg_quality = 10;
  config.fb_count = 1;
  Serial.println("Found PSRAM");

// Init camera
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

// Init TCP connection
  if (!client.connect(host, port)) {
    Serial.println("Connection to host failed");
    delay(1000);
    return;
  }


// Init SD
  if(!SD_MMC.begin()){
    Serial.println("Card Mount Failed");
    return;
  }
  uint8_t cardType = SD_MMC.cardType();
  if(cardType == CARD_NONE){
    Serial.println("No SD_MMC card attached");
    return;
  }

// Init FTP Server
  ftpSrv.begin("esp32","esp32");
}

//=======================================================================
//                    Main Program Loop
//=======================================================================
void loop() {

// Repeatedly check WiFi status
  if(!client.connected()){
    if (!client.connect(host, port)) {
      Serial.println("Connection to host failed");
      delay(1000);
      return;
    }
  }

// Handle FTP calls
  ftpSrv.handleFTP();

// Check crossing of photoelectric barrier. Not possible with interrupt 
// due toe interference with camera.
  if(digitalRead(PE_BARRIER) == LOW){
    triggeredPE = true;
    millisCrossing = millis();
  }

// Handling of triggered PE barrier
  if(triggeredPE == true){

    // capture camera frame
    digitalWrite(LED_BUILTIN, HIGH);
    delay(100);
    camera_fb_t *fb = esp_camera_fb_get();
    delay(100);
    digitalWrite(LED_BUILTIN, LOW);

    if(!fb) {
      Serial.println("Camera capture failed");
      return;
    } 
    else {
      // Path where new picture will be saved in SD Card
      String path = ("/picture_" + String(millisCrossing) +".jpg"); 
      fs::FS &fs = SD_MMC;      
      File file = fs.open(path, FILE_WRITE);
      if(!file){
          Serial.println("Failed to open file for writing");
          return;
      }
      else {
        file.write(fb->buf, fb->len);
        Serial.println("File written");
      }

      file.close();
      Serial.printf("Saved file to path: %s\n", path.c_str());
    }

    client.println(ultoa(millisCrossing,timeA,10));
    Serial.println(fb->len);
    //client.write((unsigned char *)fb->buf, fb->len);
    esp_camera_fb_return(fb);
    Serial.println("Done");
    delay(2000);
    triggeredPE = false;
  }
}
